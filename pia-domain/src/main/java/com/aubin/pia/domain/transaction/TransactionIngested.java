package com.aubin.pia.domain.transaction;

import java.time.Instant;

import com.aubin.pia.domain.shared.DomainEvent;

public record TransactionIngested(TransactionId transactionId, Instant occurredAt)
        implements DomainEvent {
    public TransactionIngested(TransactionId transactionId) {
        this(transactionId, Instant.now());
    }
}
