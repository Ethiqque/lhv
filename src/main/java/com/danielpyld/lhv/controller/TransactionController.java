package com.danielpyld.lhv.controller;

import com.danielpyld.lhv.dto.TransactionDTO;
import com.danielpyld.lhv.service.transaction.TransactionServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing portfolio-related operations.
 */
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionServiceImpl transactionServiceImpl;

    public TransactionController(TransactionServiceImpl transactionServiceImpl) {
        this.transactionServiceImpl = transactionServiceImpl;
    }

    /**
     * Retrieves all transactions in the portfolio.
     *
     * @return A ResponseEntity containing the list of all transactions and HTTP status OK.
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionServiceImpl.getAllTransactions();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    /**
     * Adds a new transaction to the portfolio.
     *
     * @param transactionDTO The TransactionDTO object representing the new transaction.
     * @return A ResponseEntity containing the added transaction and HTTP status CREATED.
     */
    @PostMapping("/addTransaction")
    public ResponseEntity<TransactionDTO> addTransaction(@RequestBody TransactionDTO transactionDTO) {
        TransactionDTO addedTransaction = transactionServiceImpl.addTransaction(transactionDTO);
        return new ResponseEntity<>(addedTransaction, HttpStatus.CREATED);
    }

    /**
     * Updates an existing transaction in the portfolio.
     *
     * @param transactionId   The Long ID of the transaction to update.
     * @param transactionDTO  The TransactionDTO object representing the updated transaction.
     * @return A ResponseEntity containing the updated transaction and HTTP status OK, or HTTP status NOT_FOUND if the transaction does not exist.
     */
    @PutMapping("/updateTransaction/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable("id") Long transactionId, @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO updatedTransaction = transactionServiceImpl.updateTransaction(transactionId, transactionDTO);
        return new ResponseEntity<>(updatedTransaction, HttpStatus.OK);
    }

    /**
     * Deletes a transaction from the portfolio.
     *
     * @param transactionId The Long ID of the transaction to delete.
     * @return A ResponseEntity with HTTP status NO_CONTENT if the transaction was deleted, or HTTP status NOT_FOUND if the transaction does not exist.
     */
    @DeleteMapping("/deleteTransaction/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("id") Long transactionId) {
        transactionServiceImpl.deleteTransaction(transactionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Retrieves transactions within a specific date range.
     *
     * @param start The start date (ISO-8601 format).
     * @param end   The end date (ISO-8601 format).
     * @return A ResponseEntity containing the list of transactions within the date range and HTTP status OK.
     */
    @GetMapping("/transactionsInDateRange")
    public ResponseEntity<List<TransactionDTO>> getTransactionsInDateRange(@RequestParam String start, @RequestParam String end) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        List<TransactionDTO> transactions = transactionServiceImpl.getTransactionsInDateRange(startDate, endDate);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    /**
     * Retrieves a single transaction by its ID.
     *
     * @param transactionId The Long ID of the transaction.
     * @return A ResponseEntity containing the transaction and HTTP status OK, or HTTP status NOT_FOUND if the transaction does not exist.
     */
    @GetMapping("/transaction/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable("id") Long transactionId) {
        return transactionServiceImpl.findTransactionById(transactionId)
                .map(transactionDTO -> new ResponseEntity<>(transactionDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
