package com.aubin.pia.simulator.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Outbound SQS message payload produced by the simulator. */
public record PaymentEventDto(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("occurredAt") Instant occurredAt,
        @JsonProperty("transaction") TransactionDto transaction,
        @JsonProperty("metadata") MetadataDto metadata) {

    public record TransactionDto(
            @JsonProperty("id") String id,
            @JsonProperty("amount") AmountDto amount,
            @JsonProperty("cardReference") CardReferenceDto cardReference,
            @JsonProperty("merchant") MerchantDto merchant,
            @JsonProperty("ipGeo") IpGeoDto ipGeo,
            @JsonProperty("status") String status) {}

    public record AmountDto(
            @JsonProperty("value") BigDecimal value, @JsonProperty("currency") String currency) {}

    public record CardReferenceDto(
            @JsonProperty("hash") String hash, @JsonProperty("last4") String last4) {}

    public record MerchantDto(
            @JsonProperty("id") String id,
            @JsonProperty("mcc") String mcc,
            @JsonProperty("country") String country) {}

    public record IpGeoDto(
            @JsonProperty("country") String country, @JsonProperty("city") String city) {}

    public record MetadataDto(@JsonProperty("correlationId") String correlationId) {}
}
