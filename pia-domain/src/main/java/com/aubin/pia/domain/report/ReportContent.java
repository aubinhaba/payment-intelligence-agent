package com.aubin.pia.domain.report;

import java.util.Objects;

public record ReportContent(String summary, String markdownBody) {
    public ReportContent {
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(markdownBody, "markdownBody must not be null");
        if (summary.isBlank()) {
            throw new IllegalArgumentException("summary must not be blank");
        }
    }
}
