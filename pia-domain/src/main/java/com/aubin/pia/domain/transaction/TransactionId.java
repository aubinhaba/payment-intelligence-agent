package com.aubin.pia.domain.transaction;

import java.util.Objects;

public record TransactionId(String value) {
    public TransactionId {
        Objects.requireNonNull(value, "TransactionId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TransactionId value must not be blank");
        }
    }
}
