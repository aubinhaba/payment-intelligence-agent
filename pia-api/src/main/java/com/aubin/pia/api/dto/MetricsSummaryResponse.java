package com.aubin.pia.api.dto;

import java.util.Map;

import com.aubin.pia.application.usecase.MetricsSummaryUseCase.Summary;

public record MetricsSummaryResponse(
        int transactionCount,
        int anomalyCount,
        int reportCount,
        Map<String, Long> anomaliesBySeverity) {

    public MetricsSummaryResponse {
        anomaliesBySeverity = anomaliesBySeverity == null ? null : Map.copyOf(anomaliesBySeverity);
    }

    public static MetricsSummaryResponse from(Summary summary) {
        return new MetricsSummaryResponse(
                summary.transactionCount(),
                summary.anomalyCount(),
                summary.reportCount(),
                summary.anomaliesBySeverity());
    }
}
