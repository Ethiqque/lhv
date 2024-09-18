package com.danielpyld.lhv.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "dividends")
public class Dividend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amountPerUnit;

    private Instant exDividendDate;

    private Instant paymentDate;

    public Dividend() {
    }

    public Dividend(BigDecimal amountPerUnit, Instant exDividendDate, Instant paymentDate) {
        this.amountPerUnit = amountPerUnit;
        this.exDividendDate = exDividendDate;
        this.paymentDate = paymentDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmountPerUnit() {
        return amountPerUnit;
    }

    public void setAmountPerUnit(BigDecimal amountPerUnit) {
        this.amountPerUnit = amountPerUnit;
    }

    public Instant getExDividendDate() {
        return exDividendDate;
    }

    public void setExDividendDate(Instant exDividendDate) {
        this.exDividendDate = exDividendDate;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }
}
