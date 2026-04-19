package com.aubin.pia.domain.transaction;

import java.util.Objects;

/** Tokenized card identifier — never holds a real PAN. */
public record CardReference(String hash, String last4) {
    public CardReference {
        Objects.requireNonNull(hash, "CardReference hash must not be null");
        Objects.requireNonNull(last4, "CardReference last4 must not be null");
        if (hash.isBlank()) {
            throw new IllegalArgumentException("CardReference hash must not be blank");
        }
        if (!last4.matches("\\d{4}")) {
            throw new IllegalArgumentException("last4 must be exactly 4 digits");
        }
    }
}
