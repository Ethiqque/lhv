package com.danielpyld.lhv.service.portfolio;

import com.danielpyld.lhv.entity.Dividend;
import com.danielpyld.lhv.entity.Profit;
import com.danielpyld.lhv.entity.Transaction;

import java.util.List;

public interface PortfolioService {
    Profit calculateProfit(List<Transaction> transactions, List<Dividend> dividends);
}
