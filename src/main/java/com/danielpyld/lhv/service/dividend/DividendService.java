package com.danielpyld.lhv.service.dividend;

import com.danielpyld.lhv.entity.Dividend;
import com.danielpyld.lhv.entity.Transaction;

import java.util.List;

public interface DividendService {
    List<Dividend> generateDividends(List<Transaction> transactions);
}
