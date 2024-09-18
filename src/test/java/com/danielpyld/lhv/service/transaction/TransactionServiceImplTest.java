package com.danielpyld.lhv.service.transaction;

import com.danielpyld.lhv.dto.TransactionDTO;
import com.danielpyld.lhv.entity.Transaction;
import com.danielpyld.lhv.entity.Type;
import com.danielpyld.lhv.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateTransactions() {
        when(transactionRepository.saveAll(anyList())).thenReturn(Arrays.asList(
                new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), Instant.now()),
                new Transaction(Type.SELL, 5, BigDecimal.valueOf(120), BigDecimal.valueOf(1), Instant.now())
        ));

        List<Transaction> transactions = transactionService.generateTransactions(2);

        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        verify(transactionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testFindTransactionById() {
        Transaction transaction = new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), Instant.now());
        transaction.setId(1L);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Optional<TransactionDTO> result = transactionService.findTransactionById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllTransactions() {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(
                new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), Instant.now()),
                new Transaction(Type.SELL, 5, BigDecimal.valueOf(120), BigDecimal.valueOf(1), Instant.now())
        ));

        List<TransactionDTO> transactions = transactionService.getAllTransactions();

        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void testAddTransaction() {
        TransactionDTO dto = new TransactionDTO();
        dto.setType(Type.BUY);
        dto.setQuantity(10);
        dto.setPrice(BigDecimal.valueOf(100));
        dto.setFee(BigDecimal.valueOf(1));
        dto.setTimestamp(LocalDateTime.now());

        Transaction transaction = new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), Instant.now());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionDTO result = transactionService.addTransaction(dto);

        assertNotNull(result);
        assertEquals(Type.BUY, result.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testUpdateTransaction() {
        TransactionDTO dto = new TransactionDTO();
        dto.setType(Type.SELL);
        dto.setQuantity(5);
        dto.setPrice(BigDecimal.valueOf(110));
        dto.setFee(BigDecimal.valueOf(1));
        dto.setTimestamp(LocalDateTime.now());

        Transaction existingTransaction = new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), Instant.now());
        existingTransaction.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);

        TransactionDTO result = transactionService.updateTransaction(1L, dto);

        assertNotNull(result);
        assertEquals(Type.SELL, result.getType());
        assertEquals(5, result.getQuantity());
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(existingTransaction);
    }

    @Test
    void testDeleteTransaction() {
        Transaction transaction = new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), Instant.now());
        transaction.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        doNothing().when(transactionRepository).delete(transaction);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).delete(transaction);
    }

    @Test
    void testGetTransactionsInDateRange() {
        Instant now = Instant.now();
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(
                new Transaction(Type.BUY, 10, BigDecimal.valueOf(100), BigDecimal.valueOf(1), now.minus(60, ChronoUnit.DAYS)),
                new Transaction(Type.SELL, 5, BigDecimal.valueOf(110), BigDecimal.valueOf(1), now.minus(30, ChronoUnit.DAYS))
        ));

        LocalDateTime start = LocalDateTime.now().minusDays(90);
        LocalDateTime end = LocalDateTime.now().minusDays(30);

        List<TransactionDTO> transactions = transactionService.getTransactionsInDateRange(start, end);

        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        verify(transactionRepository, times(1)).findAll();
    }
}
