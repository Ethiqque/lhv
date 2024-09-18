package com.danielpyld.lhv.service.dividend;

import com.danielpyld.lhv.entity.Dividend;
import com.danielpyld.lhv.entity.Transaction;
import com.danielpyld.lhv.entity.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DividendServiceImplTest {

    @InjectMocks
    private DividendServiceImpl dividendService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateDividendsWithEmptyTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        List<Dividend> dividends = dividendService.generateDividends(transactions);
        assertTrue(dividends.isEmpty(), "Dividends should be empty when no transactions are provided.");
    }

    @Test
    void testGenerateDividendsWithMultipleTransactions() {
        Instant now = Instant.now();
        List<Transaction> transactions = List.of(
                new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), now.minus(365, ChronoUnit.DAYS)),
                new Transaction(Type.BUY, 5, BigDecimal.valueOf(120), BigDecimal.valueOf(1), now.minus(180, ChronoUnit.DAYS))
        );
        List<Dividend> dividends = dividendService.generateDividends(transactions);
        assertTrue(dividends.size() > 0, "Dividends should be generated for valid transactions.");
        assertEquals(2, dividends.size(), "The number of dividends should match the number of dividend periods.");
    }

    @Test
    void testGeneratedDividendValuesAndDates() {
        Instant now = Instant.now();
        List<Transaction> transactions = List.of(
                new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), now.minus(365, ChronoUnit.DAYS)),
                new Transaction(Type.SELL, 5, BigDecimal.valueOf(120), BigDecimal.valueOf(1), now.minus(180, ChronoUnit.DAYS))
        );
        List<Dividend> dividends = dividendService.generateDividends(transactions);
        assertFalse(dividends.isEmpty(), "Dividends should be generated for the transactions.");

        for (Dividend dividend : dividends) {
            assertTrue(dividend.getAmountPerUnit().compareTo(BigDecimal.valueOf(0.5)) >= 0, "Dividend amount should be at least 0.5.");
            assertTrue(dividend.getAmountPerUnit().compareTo(BigDecimal.valueOf(2.0)) <= 0, "Dividend amount should be at most 2.0.");
            assertEquals(dividend.getExDividendDate().plus(10, ChronoUnit.DAYS), dividend.getPaymentDate(), "Payment date should be 10 days after ex-dividend date.");
        }
    }

    @Test
    void testGenerateDividendsWithInsufficientTimeRange() {
        Instant now = Instant.now();
        List<Transaction> transactions = List.of(
                new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), now.minus(30, ChronoUnit.DAYS))
        );
        List<Dividend> dividends = dividendService.generateDividends(transactions);
        assertTrue(dividends.isEmpty(), "No dividends should be generated if the time range is less than the dividend period.");
    }
}
