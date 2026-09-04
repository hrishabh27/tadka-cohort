package com.tadka.api.domain.valueobjects;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Money {
    private BigDecimal amount;
    private String currency;

    protected Money() {}

    public Money(BigDecimal amount, String currency) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "INR";
    }

    public static Money inr(BigDecimal amount) {
        return new Money(amount, "INR");
    }

    public static Money inr(double amount) {
        return new Money(BigDecimal.valueOf(amount), "INR");
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Money add(Money other) {
        if (!this.currency.equalsIgnoreCase(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies: " + this.currency + " and " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return currency + " " + amount;
    }
}
