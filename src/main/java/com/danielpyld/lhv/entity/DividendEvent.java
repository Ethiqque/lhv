package com.danielpyld.lhv.entity;

public class DividendEvent extends Event {

    private final Dividend dividend;

    public DividendEvent(Dividend dividend) {
        super(dividend.getPaymentDate());
        this.dividend = dividend;
    }

    public Dividend getDividend() {
        return dividend;
    }
}

