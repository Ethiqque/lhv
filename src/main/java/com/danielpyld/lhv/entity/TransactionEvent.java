package com.danielpyld.lhv.entity;

public class TransactionEvent extends Event {

    private final Transaction transaction;

    public TransactionEvent(Transaction transaction) {
        super(transaction.getTimestamp());
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}

