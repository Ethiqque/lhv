package com.danielpyld.lhv.service.transaction;

import com.danielpyld.lhv.dto.TransactionDTO;
import com.danielpyld.lhv.entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionService {

    List<Transaction> generateTransactions(int numTransactions);

    Optional<TransactionDTO> findTransactionById(Long transactionId);

    List<TransactionDTO> getAllTransactions();

    TransactionDTO addTransaction(TransactionDTO transactionDTO);

    TransactionDTO updateTransaction(Long transactionId, TransactionDTO transactionDTO);

    void deleteTransaction(Long transactionId);

    List<TransactionDTO> getTransactionsInDateRange(LocalDateTime start, LocalDateTime end);
}
