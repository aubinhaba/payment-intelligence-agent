package com.aubin.pia.domain.report;

import java.time.Instant;
import java.util.Objects;

import com.aubin.pia.domain.shared.AggregateRoot;
import com.aubin.pia.domain.transaction.TransactionId;

public class Report extends AggregateRoot {
    private final ReportId id;
    private final TransactionId transactionId;
    private final ReportContent content;
    private final Instant generatedAt;

    private Report(
            ReportId id, TransactionId transactionId, ReportContent content, Instant generatedAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.content = content;
        this.generatedAt = generatedAt;
    }

    public static Report generate(ReportId id, TransactionId transactionId, ReportContent content) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(content, "content must not be null");
        return new Report(id, transactionId, content, Instant.now());
    }

    public ReportId id() {
        return id;
    }

    public TransactionId transactionId() {
        return transactionId;
    }

    public ReportContent content() {
        return content;
    }

    public Instant generatedAt() {
        return generatedAt;
    }
}
