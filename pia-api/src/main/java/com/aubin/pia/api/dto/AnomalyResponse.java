package com.aubin.pia.api.dto;

import java.time.Instant;

import com.aubin.pia.domain.anomaly.Anomaly;

public record AnomalyResponse(
        String id,
        String transactionId,
        String type,
        String severity,
        String description,
        Instant detectedAt) {

    public static AnomalyResponse from(Anomaly anomaly) {
        return new AnomalyResponse(
                anomaly.id().value(),
                anomaly.transactionId().value(),
                anomaly.type().name(),
                anomaly.severity().name(),
                anomaly.description(),
                anomaly.detectedAt());
    }
}
