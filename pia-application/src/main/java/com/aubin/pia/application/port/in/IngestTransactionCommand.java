package com.aubin.pia.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record IngestTransactionCommand(
        String transactionId,
        BigDecimal amount,
        String currency,
        String cardHash,
        String cardLast4,
        String merchantId,
        String merchantMcc,
        String merchantCountry,
        String status,
        Instant occurredAt) {
    public IngestTransactionCommand {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(cardHash, "cardHash must not be null");
        Objects.requireNonNull(cardLast4, "cardLast4 must not be null");
        Objects.requireNonNull(merchantId, "merchantId must not be null");
        Objects.requireNonNull(merchantMcc, "merchantMcc must not be null");
        Objects.requireNonNull(merchantCountry, "merchantCountry must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
