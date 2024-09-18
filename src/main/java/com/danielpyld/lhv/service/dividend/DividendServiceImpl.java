package com.danielpyld.lhv.service.dividend;

import com.danielpyld.lhv.entity.Dividend;
import com.danielpyld.lhv.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DividendServiceImpl implements DividendService {

    private static final Logger logger = LoggerFactory.getLogger(DividendServiceImpl.class);

    /**
     * Generates dividend payments based on the given transactions.
     *
     * @param transactions List of transactions for which dividends should be generated.
     * @return List of generated dividends.
     */
    @Override
    public List<Dividend> generateDividends(List<Transaction> transactions) {
        List<Dividend> dividends = new ArrayList<>();
        Random rand = new Random();

        if (transactions.isEmpty()) {
            logger.warn("No transactions provided for dividend generation.");
            return dividends;
        }

        Instant startDate = transactions.get(0).getTimestamp();
        Instant endDate = transactions.get(transactions.size() - 1).getTimestamp();
        Instant exDividendDate = startDate.plus(90, ChronoUnit.DAYS);

        logger.info("Generating dividends from {} to {}", startDate, endDate);

        while (exDividendDate.isBefore(endDate)) {
            BigDecimal amountPerUnit = BigDecimal.valueOf(0.5 + (2.0 - 0.5) * rand.nextDouble()).setScale(2, RoundingMode.HALF_UP);
            Instant paymentDate = exDividendDate.plus(10, ChronoUnit.DAYS);

            dividends.add(new Dividend(amountPerUnit, exDividendDate, paymentDate));
            logger.info("Generated dividend: {} on ex-dividend date: {} with payment date: {}", amountPerUnit, exDividendDate, paymentDate);

            exDividendDate = exDividendDate.plus(90, ChronoUnit.DAYS);
        }

        logger.info("Dividend generation complete. Total dividends generated: {}", dividends.size());
        return dividends;
    }
}
