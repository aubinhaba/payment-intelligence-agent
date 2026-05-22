package com.aubin.pia.infrastructure.messaging.sqs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aubin.pia.application.port.out.AnalysisRequestPublisher;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.application.usecase.DetectAnomaliesUseCase;
import com.aubin.pia.application.usecase.IngestTransactionUseCase;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.AmountDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.CardReferenceDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.MerchantDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.MetadataDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.TransactionDto;

@ExtendWith(MockitoExtension.class)
class SqsPaymentEventListenerTest {

    @Mock private IngestTransactionUseCase ingestTransactionUseCase;
    @Mock private DetectAnomaliesUseCase detectAnomaliesUseCase;
    @Mock private AnalysisRequestPublisher analysisRequestPublisher;
    @Mock private TransactionRepository transactionRepository;
    @Mock private MetricsPublisher metricsPublisher;
    @Mock private Transaction existingTransaction;

    private SqsPaymentEventListener listener;

    @BeforeEach
    void setUp() {
        listener =
                new SqsPaymentEventListener(
                        ingestTransactionUseCase,
                        detectAnomaliesUseCase,
                        analysisRequestPublisher,
                        transactionRepository,
                        metricsPublisher,
                        Severity.HIGH);
    }

    @Test
    void handle_valid_event_calls_ingest_and_detect() {
        when(transactionRepository.findById(any())).thenReturn(Optional.empty());
        when(detectAnomaliesUseCase.detect(any())).thenReturn(List.of());

        listener.handle(buildEvent("tx-new-001"));

        verify(ingestTransactionUseCase).ingest(any());
        verify(detectAnomaliesUseCase).detect(any());
        verify(analysisRequestPublisher, never()).publish(any());
    }

    @Test
    void handle_duplicate_event_skips_ingest() {
        when(transactionRepository.findById(any())).thenReturn(Optional.of(existingTransaction));

        listener.handle(buildEvent("tx-dup-001"));

        verify(ingestTransactionUseCase, never()).ingest(any());
        verify(detectAnomaliesUseCase, never()).detect(any());
        verify(analysisRequestPublisher, never()).publish(any());
    }

    @Test
    void handle_high_severity_anomaly_publishes_analysis_request() {
        when(transactionRepository.findById(any())).thenReturn(Optional.empty());
        when(detectAnomaliesUseCase.detect(any()))
                .thenReturn(List.of(anomaly("tx-high-001", Severity.HIGH)));

        listener.handle(buildEvent("tx-high-001"));

        verify(analysisRequestPublisher).publish(new TransactionId("tx-high-001"));
    }

    @Test
    void handle_critical_severity_anomaly_publishes_analysis_request() {
        when(transactionRepository.findById(any())).thenReturn(Optional.empty());
        when(detectAnomaliesUseCase.detect(any()))
                .thenReturn(List.of(anomaly("tx-crit-001", Severity.CRITICAL)));

        listener.handle(buildEvent("tx-crit-001"));

        verify(analysisRequestPublisher).publish(new TransactionId("tx-crit-001"));
    }

    @Test
    void handle_medium_severity_anomaly_does_not_publish() {
        when(transactionRepository.findById(any())).thenReturn(Optional.empty());
        when(detectAnomaliesUseCase.detect(any()))
                .thenReturn(List.of(anomaly("tx-med-001", Severity.MEDIUM)));

        listener.handle(buildEvent("tx-med-001"));

        verify(analysisRequestPublisher, never()).publish(any());
    }

    @Test
    void handle_mixed_severities_publishes_when_max_meets_threshold() {
        when(transactionRepository.findById(any())).thenReturn(Optional.empty());
        when(detectAnomaliesUseCase.detect(any()))
                .thenReturn(
                        List.of(
                                anomaly("tx-mixed-001", Severity.LOW),
                                anomaly("tx-mixed-001", Severity.HIGH)));

        listener.handle(buildEvent("tx-mixed-001"));

        verify(analysisRequestPublisher).publish(new TransactionId("tx-mixed-001"));
    }

    private static Anomaly anomaly(String txId, Severity severity) {
        return Anomaly.detect(
                new AnomalyId("a_" + txId + "_" + severity.name()),
                new TransactionId(txId),
                AnomalyType.AMOUNT,
                severity,
                "test anomaly");
    }

    private static PaymentEventDto buildEvent(String txId) {
        return new PaymentEventDto(
                "evt-001",
                "PAYMENT_AUTHORIZED",
                Instant.now(),
                new TransactionDto(
                        txId,
                        new AmountDto(new BigDecimal("99.99"), "EUR"),
                        new CardReferenceDto("card-hash-test", "1234"),
                        new MerchantDto("m_test", "5812", "FR"),
                        "AUTHORIZED"),
                new MetadataDto("corr-001"));
    }
}
