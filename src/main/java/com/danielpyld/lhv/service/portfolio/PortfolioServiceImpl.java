package com.danielpyld.lhv.service.portfolio;

import com.danielpyld.lhv.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Value("${scale}")
    private int SCALE;
    private BigDecimal realizedStockProfit = BigDecimal.ZERO;
    private static final Logger logger = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    /**
     * Calculates the profit based on a list of transactions and dividends.
     *
     * @param transactions List of transaction events (buy/sell).
     * @param dividends    List of dividend events.
     * @return ProfitResult containing calculated profit and loss details.
     */
    @Override
    public Profit calculateProfit(List<Transaction> transactions, List<Dividend> dividends) {
        logger.info("Calculating profit for transactions and dividends.");
        List<Event> events = prepareEvents(transactions, dividends);
        Collections.sort(events);

        Queue<Transaction> buys = new LinkedList<>();
        BigDecimal dividendProfit = BigDecimal.ZERO;
        Instant currentDateTime = Instant.now();
        BigDecimal lastPrice = BigDecimal.ZERO;

        for (Event event : events) {
            if (event instanceof TransactionEvent) {
                lastPrice = handleTransactionEvent((TransactionEvent) event, buys);
            } else if (event instanceof DividendEvent) {
                dividendProfit = handleDividendEvent((DividendEvent) event, currentDateTime, transactions);
            }
        }

        int remainingHoldings = calculateRemainingHoldings(buys);
        realizedStockProfit = calculateRealizedProfit(transactions, buys);

        BigDecimal unrealizedGains = findUnrealizedGains(remainingHoldings, buys, lastPrice);
        BigDecimal totalProfit = realizedStockProfit.add(dividendProfit);

        logger.info("Total profit calculated: {}", totalProfit);
        return createProfitResult(totalProfit, realizedStockProfit, dividendProfit, unrealizedGains);
    }

    /**
     * Prepares a list of events from transactions and dividends.
     *
     * @param transactions List of transactions.
     * @param dividends    List of dividends.
     * @return List of prepared events.
     */
    private List<Event> prepareEvents(List<Transaction> transactions, List<Dividend> dividends) {
        List<Event> events = new ArrayList<>();
        for (Transaction tx : transactions) {
            events.add(new TransactionEvent(tx));
        }
        for (Dividend div : dividends) {
            events.add(new DividendEvent(div));
        }
        return events;
    }

    /**
     * Handles transaction events and updates the queue of buy transactions.
     *
     * @param event Transaction event to handle.
     * @param buys  Queue of buy transactions.
     * @return The last price of the transaction.
     */
    BigDecimal handleTransactionEvent(TransactionEvent event, Queue<Transaction> buys) {
        Transaction transaction = event.getTransaction();
        BigDecimal lastPrice = transaction.getPrice();

        if (transaction.getType() == Type.BUY) {
            buys.offer(transaction);
            logger.info("Buy transaction added: {}", transaction);
        } else {
            processSellTransaction(transaction, buys);
        }

        return lastPrice;
    }

    /**
     * Processes sell transactions and updates the realized profit.
     *
     * @param transaction Sell transaction to process.
     * @param buys        Queue of buy transactions.
     */
    private void processSellTransaction(Transaction transaction, Queue<Transaction> buys) {
        int quantityToSell = transaction.getQuantity();
        BigDecimal totalSellFee = transaction.getFee();
        BigDecimal realizedProfit = BigDecimal.ZERO;

        while (quantityToSell > 0 && !buys.isEmpty()) {
            Transaction buy = buys.peek();
            int quantityAvailable = buy.getQuantity();
            int quantitySold = Math.min(quantityAvailable, quantityToSell);

            BigDecimal sellFeeProportion = calculateProportionalAmount(totalSellFee, quantitySold, transaction.getQuantity());
            BigDecimal buyFeeProportion = calculateProportionalAmount(buy.getFee(), quantitySold, buy.getQuantity());

            buy.setFee(buy.getFee().subtract(buyFeeProportion));
            BigDecimal totalBuyAmount = buy.getPrice().multiply(BigDecimal.valueOf(quantitySold)).add(buyFeeProportion);
            BigDecimal totalSellAmount = transaction.getPrice().multiply(BigDecimal.valueOf(quantitySold)).subtract(sellFeeProportion);

            realizedProfit = realizedProfit.add(totalSellAmount.subtract(totalBuyAmount));

            buy.setQuantity(quantityAvailable - quantitySold);
            quantityToSell -= quantitySold;

            if (buy.getQuantity() == 0) {
                buys.poll();
            }
        }

        realizedStockProfit = realizedStockProfit.add(realizedProfit);
        logger.info("Sell transaction processed: {} - Realized profit: {}", transaction, realizedProfit);
    }

    /**
     * Calculates the total realized profit from sell transactions.
     *
     * @param transactions List of transactions.
     * @param buys        Queue of buy transactions.
     * @return Total realized profit.
     */
    BigDecimal calculateRealizedProfit(List<Transaction> transactions, Queue<Transaction> buys) {
        BigDecimal realizedProfit = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction.getType() == Type.SELL) {
                BigDecimal totalSellAmount = transaction.getPrice().multiply(BigDecimal.valueOf(transaction.getQuantity()));
                BigDecimal totalSellFee = transaction.getFee();
                BigDecimal netSellAmount = totalSellAmount.subtract(totalSellFee);

                int quantityToSell = transaction.getQuantity();
                while (quantityToSell > 0 && !buys.isEmpty()) {
                    Transaction buy = buys.peek();
                    int quantityAvailable = buy.getQuantity();
                    int quantitySold = Math.min(quantityAvailable, quantityToSell);

                    BigDecimal buyCost = buy.getPrice().multiply(BigDecimal.valueOf(quantitySold));
                    BigDecimal buyFee = calculateProportionalAmount(buy.getFee(), quantitySold, buy.getQuantity());
                    BigDecimal totalBuyAmount = buyCost.add(buyFee);

                    realizedProfit = realizedProfit.add(netSellAmount.subtract(totalBuyAmount));

                    buy.setQuantity(quantityAvailable - quantitySold);
                    quantityToSell -= quantitySold;

                    if (buy.getQuantity() == 0) {
                        buys.poll();
                    }
                }
            }
        }

        return realizedProfit;
    }

    /**
     * Handles dividend payment events and calculates the dividend amount.
     *
     * @param event          Dividend payment event to handle.
     * @param currentDateTime Current date and time for comparison.
     * @param transactions    List of transactions.
     * @return Dividend amount for the event.
     */
    private BigDecimal handleDividendEvent(DividendEvent event, Instant currentDateTime, List<Transaction> transactions) {
        Dividend dividend = event.getDividend();
        if (!dividend.getPaymentDate().isAfter(currentDateTime)) {
            int holdingsAtExDate = calculateHoldingsAtDate(transactions, dividend.getExDividendDate());
            return dividend.getAmountPerUnit().multiply(BigDecimal.valueOf(holdingsAtExDate));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculates the remaining holdings based on buy transactions in the queue.
     *
     * @param buys Queue of buy transactions.
     * @return Total remaining holdings.
     */
    private int calculateRemainingHoldings(Queue<Transaction> buys) {
        int remainingHoldings = 0;
        for (Transaction buy : buys) {
            remainingHoldings += buy.getQuantity();
        }
        return remainingHoldings;
    }

    /**
     * Creates a Profit object from the calculated profit details.
     *
     * @param totalProfit       Total profit calculated.
     * @param realizedStockProfit Realized stock profit.
     * @param dividendProfit      Total dividend profit.
     * @param unrealizedGains    Total unrealized gains.
     * @return Profit object containing profit details.
     */
    private Profit createProfitResult(BigDecimal totalProfit, BigDecimal realizedStockProfit, BigDecimal dividendProfit, BigDecimal unrealizedGains) {
        return new Profit(
                totalProfit.setScale(SCALE, RoundingMode.HALF_UP),
                realizedStockProfit.setScale(SCALE, RoundingMode.HALF_UP),
                dividendProfit.setScale(SCALE, RoundingMode.HALF_UP),
                unrealizedGains.setScale(SCALE, RoundingMode.HALF_UP)
        );
    }

    /**
     * Calculates the proportional amount based on a part of the total.
     *
     * @param totalAmount Total amount to divide.
     * @param part       Part of the total amount.
     * @param total      Total for proportional calculation.
     * @return Proportional amount.
     */
    private BigDecimal calculateProportionalAmount(BigDecimal totalAmount, int part, int total) {
        return totalAmount.multiply(BigDecimal.valueOf(part))
                .divide(BigDecimal.valueOf(total), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the total holdings at a specific date based on transactions.
     *
     * @param transactions List of transactions.
     * @param date        Date to check holdings.
     * @return Total holdings at the specified date.
     */
    private int calculateHoldingsAtDate(List<Transaction> transactions, Instant date) {
        int holdings = 0;
        for (Transaction tx : transactions) {
            if (!tx.getTimestamp().isAfter(date)) {
                holdings += tx.getType() == Type.BUY ? tx.getQuantity() : -tx.getQuantity();
            } else {
                break;
            }
        }
        return holdings;
    }

    /**
     * Calculates unrealized gains based on remaining holdings and buy transactions.
     *
     * @param remainingHoldings Remaining holdings.
     * @param buys             Queue of buy transactions.
     * @param lastPrice        Last price of the stock.
     * @return Total unrealized gains.
     */
    BigDecimal findUnrealizedGains(int remainingHoldings, Queue<Transaction> buys, BigDecimal lastPrice) {
        BigDecimal unrealizedGains = BigDecimal.ZERO;
        if (remainingHoldings > 0) {
            BigDecimal totalCost = BigDecimal.ZERO;
            for (Transaction buy : buys) {
                BigDecimal buyCost = buy.getPrice().multiply(BigDecimal.valueOf(buy.getQuantity()));
                totalCost = totalCost.add(buyCost).add(buy.getFee());
            }
            BigDecimal marketValue = lastPrice.multiply(BigDecimal.valueOf(remainingHoldings));
            unrealizedGains = marketValue.subtract(totalCost);
        }
        return unrealizedGains;
    }
}
