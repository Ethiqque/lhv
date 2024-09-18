package com.danielpyld.lhv.controller;

import com.danielpyld.lhv.entity.Dividend;
import com.danielpyld.lhv.entity.Profit;
import com.danielpyld.lhv.entity.Transaction;
import com.danielpyld.lhv.service.dividend.DividendService;
import com.danielpyld.lhv.service.portfolio.PortfolioService;
import com.danielpyld.lhv.service.portfolio.PortfolioServiceImpl;
import com.danielpyld.lhv.service.dividend.DividendServiceImpl;
import com.danielpyld.lhv.service.transaction.TransactionService;
import com.danielpyld.lhv.service.transaction.TransactionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * REST controller for managing calculator-related operations.
 */
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

   private final PortfolioService portfolioService;
   private final DividendService dividendService;
   private final ObjectMapper objectMapper;
   private final TransactionService transactionService;

   public PortfolioController(DividendServiceImpl dividendService,
                              PortfolioServiceImpl portfolioService,
                              TransactionServiceImpl transactionService) {
      this.dividendService = dividendService;
      this.portfolioService = portfolioService;
      this.objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      this.transactionService = transactionService;
   }

   /**
    * Generate transactions and write them to a JSON file.
    *
    * @return ResponseEntity with the status of the operation.
    */
   @GetMapping("/generate")
   public ResponseEntity<String> generateTransactions() {
      try {
         List<Transaction> transactions = transactionService.generateTransactions(1000);
         objectMapper.writeValue(new File("transactions.json"), transactions);
         return new ResponseEntity<>("Transactions have been written to transactions.json", HttpStatus.OK);
      } catch (Exception e) {
         e.printStackTrace();
         return new ResponseEntity<>("Failed to generate transactions", HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }

   /**
    * Calculate profit from a given JSON file containing transactions.
    *
    * @param fileName The name of the JSON file containing the transactions.
    * @return ResponseEntity containing ProfitResult with detailed profit calculations.
    */
   @GetMapping("/calculate")
   public ResponseEntity<?> calculateProfitFromFile(@RequestParam String fileName) {
      try {
         File file = new File(fileName);
         if (!file.exists()) {
            return new ResponseEntity<>("File not found: " + fileName, HttpStatus.NOT_FOUND);
         }

         List<Transaction> transactions = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class));
         List<Dividend> dividends = dividendService.generateDividends(transactions);
         Profit profit = portfolioService.calculateProfit(transactions, dividends);

         return new ResponseEntity<>(profit, HttpStatus.OK);
      } catch (IOException e) {
         e.printStackTrace();
         return new ResponseEntity<>("Error reading file", HttpStatus.INTERNAL_SERVER_ERROR);
      }
   }

   /**
    * Generate transactions internally and calculate Profit/Loss without reading from a file.
    *
    * @return ResponseEntity containing ProfitResult.
    */
   @GetMapping("/calculate/default")
   public ResponseEntity<Profit> calculateDefaultProfit() {
      List<Transaction> transactions = transactionService.generateTransactions(1000);
      List<Dividend> dividends = dividendService.generateDividends(transactions);
      Profit profit = portfolioService.calculateProfit(transactions, dividends);
      return new ResponseEntity<>(profit, HttpStatus.OK);
   }
}
