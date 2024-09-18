package com.danielpyld.lhv.service.transaction;

import com.danielpyld.lhv.dto.TransactionDTO;
import com.danielpyld.lhv.entity.Transaction;
import com.danielpyld.lhv.entity.Type;
import com.danielpyld.lhv.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generates a list of random transactions and saves them to the database.
     *
     * @param numTransactions The number of transactions to generate.
     * @return The list of generated and saved transactions.
     */
    @Override
    public List<Transaction> generateTransactions(int numTransactions) {
        List<Transaction> transactions = new ArrayList<>();
        Random rand = new Random();

        Instant startTime = Instant.now().minus(365 * 2, ChronoUnit.DAYS);
        Instant endTime = Instant.now().minus(30, ChronoUnit.DAYS);
        long totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime);

        int maxQuantity = 100;
        BigDecimal meanPrice = new BigDecimal("100.00");
        BigDecimal stddevPrice = new BigDecimal("20.00");

        int totalHoldings = 0;
        Instant currentTimestamp = startTime;

        for (int i = 0; i < numTransactions; i++) {
            Type type;

            if (totalHoldings == 0) {
                type = Type.BUY;
            } else {
                type = rand.nextBoolean() ? Type.BUY : Type.SELL;
            }

            int quantity;
            if (type == Type.BUY) {
                quantity = rand.nextInt(maxQuantity) + 1;
                totalHoldings += quantity;
            } else {
                quantity = rand.nextInt(totalHoldings) + 1;
                totalHoldings -= quantity;
            }

            BigDecimal price = generateNormalDistributedPrice(meanPrice, stddevPrice, rand);
            BigDecimal fee = calculateCost(price.multiply(BigDecimal.valueOf(quantity)));

            long remainingMinutes = totalMinutes - ChronoUnit.MINUTES.between(startTime, currentTimestamp);
            long randomMinutes = rand.nextInt((int) remainingMinutes / (numTransactions - i));
            currentTimestamp = currentTimestamp.plus(randomMinutes, ChronoUnit.MINUTES);

            Transaction transaction = new Transaction(type, quantity, price, fee, currentTimestamp);
            transactions.add(transaction);
        }

        logger.info("Generated {} transactions", transactions.size());
        return transactionRepository.saveAll(transactions);
    }

    private BigDecimal calculateCost(BigDecimal orderCost) {
        BigDecimal fee = orderCost.multiply(new BigDecimal("0.005"));
        BigDecimal MIN_FEE = new BigDecimal("1.00");
        BigDecimal MAX_FEE = new BigDecimal("10.00");
        if (fee.compareTo(MIN_FEE) < 0) {
            fee = MIN_FEE;
        } else if (fee.compareTo(MAX_FEE) > 0) {
            fee = MAX_FEE;
        }
        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal generateNormalDistributedPrice(BigDecimal mean, BigDecimal stddev, Random rand) {
        double gaussianValue = rand.nextGaussian();
        BigDecimal price = mean.add(stddev.multiply(BigDecimal.valueOf(gaussianValue)));
        if (price.compareTo(BigDecimal.ONE) < 0) {
            price = BigDecimal.ONE;
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Finds a transaction by its ID.
     *
     * @param transactionId The Long ID of the transaction to find.
     * @return An Optional containing the TransactionDTO if found, or empty if not found.
     */
    @Override
    public Optional<TransactionDTO> findTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .map(this::convertToDTO);
    }

    /**
     * Retrieves all transactions.
     *
     * @return A list of TransactionDTO objects representing the portfolio transactions.
     */
    @Override
    public List<TransactionDTO> getAllTransactions() {
        logger.info("Retrieving all transactions.");
        return transactionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new transaction to the portfolio.
     *
     * @param transactionDTO The TransactionDTO object representing the new transaction.
     * @return The TransactionDTO object representing the added transaction.
     */
    @Override
    public TransactionDTO addTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = convertToEntity(transactionDTO);
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Added new transaction: {}", savedTransaction);
        return convertToDTO(savedTransaction);
    }

    /**
     * Updates an existing transaction in the portfolio.
     *
     * @param transactionId  The Long ID of the transaction to update.
     * @param transactionDTO The TransactionDTO object representing the updated transaction.
     * @return The TransactionDTO object representing the updated transaction.
     */
    @Override
    public TransactionDTO updateTransaction(Long transactionId, TransactionDTO transactionDTO) {
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        existingTransaction.setType(transactionDTO.getType());
        existingTransaction.setQuantity(transactionDTO.getQuantity());
        existingTransaction.setPrice(transactionDTO.getPrice());
        existingTransaction.setFee(transactionDTO.getFee());
        existingTransaction.setTimestamp(transactionDTO.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());

        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        logger.info("Updated transaction: {}", updatedTransaction);
        return convertToDTO(updatedTransaction);
    }

    /**
     * Deletes a transaction from the portfolio.
     *
     * @param transactionId The Long ID of the transaction to delete.
     */
    @Override
    public void deleteTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transactionRepository.delete(transaction);
        logger.info("Deleted transaction: {}", transaction);
    }

    /**
     * Retrieves transactions within a specific date range.
     *
     * @param start The start date.
     * @param end   The end date.
     * @return A list of TransactionDTO objects representing transactions in the specified date range.
     */
    @Override
    public List<TransactionDTO> getTransactionsInDateRange(LocalDateTime start, LocalDateTime end) {
        logger.info("Retrieving transactions between {} and {}", start, end);
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTimestamp().isAfter(start.atZone(ZoneId.systemDefault()).toInstant()) &&
                        t.getTimestamp().isBefore(end.atZone(ZoneId.systemDefault()).toInstant()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Transaction entity to a TransactionDTO object.
     *
     * @param transaction The Transaction entity to convert.
     * @return The TransactionDTO object representing the transaction.
     */
    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setQuantity(transaction.getQuantity());
        dto.setPrice(transaction.getPrice());
        dto.setFee(transaction.getFee());
        dto.setTimestamp(transaction.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDateTime());
        return dto;
    }

    /**
     * Converts a TransactionDTO object to a Transaction entity.
     *
     * @param dto The TransactionDTO object to convert.
     * @return The Transaction entity representing the transaction.
     */
    private Transaction convertToEntity(TransactionDTO dto) {
        Transaction transaction = new Transaction();
        transaction.setType(dto.getType());
        transaction.setQuantity(dto.getQuantity());
        transaction.setPrice(dto.getPrice());
        transaction.setFee(dto.getFee());
        transaction.setTimestamp(dto.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
        return transaction;
    }
}
