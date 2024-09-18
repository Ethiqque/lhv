package com.danielpyld.lhv.service.portfolio;

import com.danielpyld.lhv.entity.Transaction;
import com.danielpyld.lhv.entity.TransactionEvent;
import com.danielpyld.lhv.entity.Type;
import com.danielpyld.lhv.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PortfolioServiceImplTest {

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    @Mock
    private TransactionRepository transactionRepository;

    private int SCALE = 8;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        portfolioService = new PortfolioServiceImpl();
    }

    @Test
    public void testHandleTransactionEvent() {
        Queue<Transaction> buys = new LinkedList<>();
        TransactionEvent event = new TransactionEvent(new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.ZERO, Instant.now()));

        BigDecimal lastPrice = portfolioService.handleTransactionEvent(event, buys);

        assertEquals(BigDecimal.valueOf(100), lastPrice);
        assertEquals(1, buys.size());
    }

    @Test
    public void testCalculateRealizedProfit() {
        Queue<Transaction> buys = new LinkedList<>();
        buys.offer(new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.ZERO, Instant.now()));

        List<Transaction> transactions = Arrays.asList(
                new Transaction(Type.SELL, 5, BigDecimal.valueOf(120), BigDecimal.ZERO, Instant.now())
        );

        BigDecimal realizedProfit = portfolioService.calculateRealizedProfit(transactions, buys);

        BigDecimal expectedProfit = BigDecimal.valueOf(100).setScale(SCALE);

        assertEquals(expectedProfit, realizedProfit.setScale(SCALE));
    }


    @Test
    public void testFindUnrealizedGains() {
        Queue<Transaction> buys = new LinkedList<>();
        buys.offer(new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.ZERO, Instant.now()));

        BigDecimal unrealizedGains = portfolioService.findUnrealizedGains(10, buys, BigDecimal.valueOf(120));

        BigDecimal expectedUnrealizedGains = BigDecimal.valueOf(200).setScale(SCALE);

        assertEquals(expectedUnrealizedGains, unrealizedGains.setScale(SCALE));
    }

}
