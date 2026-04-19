package com.aubin.pia.application.port.in;

import java.util.Objects;

import com.aubin.pia.domain.transaction.TransactionId;

public record AnalyzeWithAgentCommand(TransactionId transactionId) {
    public AnalyzeWithAgentCommand {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
    }
}
