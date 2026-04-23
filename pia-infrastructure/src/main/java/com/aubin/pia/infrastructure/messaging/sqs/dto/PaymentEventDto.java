package com.aubin.pia.infrastructure.messaging.sqs.dto;

import java.time.Instant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Inbound SQS message envelope for payment events.
 *
 * <pre>
 * {
 *   "eventId": "uuid",
 *   "eventType": "PAYMENT_AUTHORIZED",
 *   "occurredAt": "2026-04-18T10:00:00Z",
 *   "transaction": { ... },
 *   "metadata": { "correlationId": "..." }
 * }
 * </pre>
 */
public record PaymentEventDto(
        @JsonProperty("eventId") @NotBlank String eventId,
        @JsonProperty("eventType") @NotBlank String eventType,
        @JsonProperty("occurredAt") @NotNull Instant occurredAt,
        @JsonProperty("transaction") @NotNull @Valid TransactionDto transaction,
        @JsonProperty("metadata") @NotNull @Valid MetadataDto metadata) {

    public record TransactionDto(
            @JsonProperty("id") @NotBlank String id,
            @JsonProperty("amount") @NotNull @Valid AmountDto amount,
            @JsonProperty("cardReference") @NotNull @Valid CardReferenceDto cardReference,
            @JsonProperty("merchant") @NotNull @Valid MerchantDto merchant,
            @JsonProperty("status") @NotBlank String status) {}

    public record AmountDto(
            @JsonProperty("value") @NotNull java.math.BigDecimal value,
            @JsonProperty("currency") @NotBlank String currency) {}

    public record CardReferenceDto(
            @JsonProperty("hash") @NotBlank String hash,
            @JsonProperty("last4") @NotBlank String last4) {}

    public record MerchantDto(
            @JsonProperty("id") @NotBlank String id,
            @JsonProperty("mcc") @NotBlank String mcc,
            @JsonProperty("country") @NotBlank String country) {}

    public record MetadataDto(@JsonProperty("correlationId") @NotBlank String correlationId) {}
}
