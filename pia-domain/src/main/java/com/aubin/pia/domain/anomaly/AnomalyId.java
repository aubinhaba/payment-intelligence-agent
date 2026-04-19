package com.aubin.pia.domain.anomaly;

import java.util.Objects;

public record AnomalyId(String value) {
    public AnomalyId {
        Objects.requireNonNull(value, "AnomalyId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AnomalyId value must not be blank");
        }
    }
}
