package com.aubin.pia.infrastructure.messaging.sqs.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Inbound/outbound SQS message envelope for anomaly analysis requests.
 *
 * <p>Published by {@code SqsPaymentEventListener} when at least one anomaly meets the configured
 * severity threshold; consumed by {@code SqsAnomalyAnalysisListener} which invokes the {@code
 * AnalyzeAndReportUseCase} pipeline.
 *
 * <pre>
 * {
 *   "transactionId": "tx_xxx",
 *   "correlationId": "...",
 *   "requestedAt": "2026-04-18T10:00:00Z"
 * }
 * </pre>
 */
public record AnomalyAnalysisRequestDto(
        @JsonProperty("transactionId") @NotBlank String transactionId,
        @JsonProperty("correlationId") @NotBlank String correlationId,
        @JsonProperty("requestedAt") @NotNull Instant requestedAt) {}
