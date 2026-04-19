package com.aubin.pia.domain.transaction;

import java.util.Objects;

public record Merchant(String id, String mcc, String country) {
    public Merchant {
        Objects.requireNonNull(id, "Merchant id must not be null");
        Objects.requireNonNull(mcc, "Merchant mcc must not be null");
        Objects.requireNonNull(country, "Merchant country must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Merchant id must not be blank");
        }
        if (!mcc.matches("\\d{4}")) {
            throw new IllegalArgumentException("MCC must be exactly 4 digits");
        }
        if (country.length() != 2) {
            throw new IllegalArgumentException("Country must be ISO 3166-1 alpha-2 (2 chars)");
        }
    }
}
