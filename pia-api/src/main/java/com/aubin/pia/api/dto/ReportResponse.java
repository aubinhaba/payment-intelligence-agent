package com.aubin.pia.api.dto;

import java.time.Instant;

import com.aubin.pia.domain.report.Report;

public record ReportResponse(
        String id, String transactionId, String summary, String markdownBody, Instant generatedAt) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.id().value(),
                report.transactionId().value(),
                report.content().summary(),
                report.content().markdownBody(),
                report.generatedAt());
    }
}
