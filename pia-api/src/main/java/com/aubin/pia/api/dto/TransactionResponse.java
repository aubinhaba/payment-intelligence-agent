package com.aubin.pia.api.dto;

import java.time.Instant;

import com.aubin.pia.domain.transaction.Transaction;

public record TransactionResponse(
        String id,
        String amount,
        String currency,
        String merchantId,
        String merchantCountry,
        String cardLast4,
        Instant occurredAt,
        String status) {

    public static TransactionResponse from(Transaction tx) {
        return new TransactionResponse(
                tx.id().value(),
                tx.amount().value().toPlainString(),
                tx.amount().currency().getCurrencyCode(),
                tx.merchant().id(),
                tx.merchant().country(),
                tx.cardReference().last4(),
                tx.occurredAt(),
                tx.status().name());
    }
}
