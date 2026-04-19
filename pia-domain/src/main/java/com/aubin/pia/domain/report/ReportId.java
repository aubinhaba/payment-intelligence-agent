package com.aubin.pia.domain.report;

import java.util.Objects;

public record ReportId(String value) {
    public ReportId {
        Objects.requireNonNull(value, "ReportId value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ReportId value must not be blank");
        }
    }
}
