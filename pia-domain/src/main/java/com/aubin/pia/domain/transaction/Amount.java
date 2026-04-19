package com.aubin.pia.domain.transaction;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Amount(BigDecimal value, Currency currency) {
    public Amount {
        Objects.requireNonNull(value, "Amount value must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }
    }

    public static Amount of(BigDecimal value, String currencyCode) {
        return new Amount(value, Currency.getInstance(currencyCode));
    }
}
