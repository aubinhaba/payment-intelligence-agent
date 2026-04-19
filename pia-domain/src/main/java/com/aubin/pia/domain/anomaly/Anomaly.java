package com.aubin.pia.domain.anomaly;

import java.time.Instant;
import java.util.Objects;

import com.aubin.pia.domain.shared.AggregateRoot;
import com.aubin.pia.domain.transaction.TransactionId;

public class Anomaly extends AggregateRoot {
    private final AnomalyId id;
    private final TransactionId transactionId;
    private final AnomalyType type;
    private final Severity severity;
    private final String description;
    private final Instant detectedAt;

    private Anomaly(
            AnomalyId id,
            TransactionId transactionId,
            AnomalyType type,
            Severity severity,
            String description,
            Instant detectedAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.type = type;
        this.severity = severity;
        this.description = description;
        this.detectedAt = detectedAt;
    }

    public static Anomaly detect(
            AnomalyId id,
            TransactionId transactionId,
            AnomalyType type,
            Severity severity,
            String description) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(severity, "severity must not be null");
        Objects.requireNonNull(description, "description must not be null");
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        Anomaly anomaly =
                new Anomaly(id, transactionId, type, severity, description, Instant.now());
        anomaly.registerEvent(new AnomalyDetected(id, transactionId, type, severity));
        return anomaly;
    }

    public AnomalyId id() {
        return id;
    }

    public TransactionId transactionId() {
        return transactionId;
    }

    public AnomalyType type() {
        return type;
    }

    public Severity severity() {
        return severity;
    }

    public String description() {
        return description;
    }

    public Instant detectedAt() {
        return detectedAt;
    }
}
