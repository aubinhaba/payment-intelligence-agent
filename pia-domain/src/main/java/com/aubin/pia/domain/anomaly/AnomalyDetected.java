package com.aubin.pia.domain.anomaly;

import java.time.Instant;

import com.aubin.pia.domain.shared.DomainEvent;
import com.aubin.pia.domain.transaction.TransactionId;

public record AnomalyDetected(
        AnomalyId anomalyId,
        TransactionId transactionId,
        AnomalyType type,
        Severity severity,
        Instant occurredAt)
        implements DomainEvent {
    public AnomalyDetected(
            AnomalyId anomalyId, TransactionId transactionId, AnomalyType type, Severity severity) {
        this(anomalyId, transactionId, type, severity, Instant.now());
    }
}
