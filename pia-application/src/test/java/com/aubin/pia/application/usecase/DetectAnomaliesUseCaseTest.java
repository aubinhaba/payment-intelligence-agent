package com.aubin.pia.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.application.port.out.EventPublisher;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.DetectionPolicy;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class DetectAnomaliesUseCaseTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AnomalyRepository anomalyRepository;
    @Mock EventPublisher eventPublisher;
    @Mock MetricsPublisher metricsPublisher;

    private static final TransactionId TX_ID = new TransactionId("tx_001");

    private static Transaction sampleTransaction() {
        return Transaction.reconstitute(
                TX_ID,
                Amount.of(new BigDecimal("9999.00"), "EUR"),
                new CardReference("hash", "4242"),
                new Merchant("m_001", "5812", "FR"),
                Instant.now(),
                TransactionStatus.AUTHORIZED);
    }

    @Test
    void should_return_empty_when_no_policy_matches() {
        when(transactionRepository.findById(TX_ID)).thenReturn(Optional.of(sampleTransaction()));
        DetectionPolicy noMatch = tx -> Optional.empty();
        DetectAnomaliesUseCase useCase =
                new DetectAnomaliesUseCase(
                        transactionRepository,
                        anomalyRepository,
                        eventPublisher,
                        metricsPublisher,
                        List.of(noMatch));

        List<Anomaly> result = useCase.detect(TX_ID);

        assertThat(result).isEmpty();
        verify(anomalyRepository, never()).save(any());
    }

    @Test
    void should_save_and_publish_detected_anomalies() {
        when(transactionRepository.findById(TX_ID)).thenReturn(Optional.of(sampleTransaction()));
        DetectionPolicy alwaysMatch =
                tx ->
                        Optional.of(
                                Anomaly.detect(
                                        new AnomalyId("a_001"),
                                        tx.id(),
                                        AnomalyType.AMOUNT,
                                        Severity.HIGH,
                                        "Amount exceeds threshold"));
        DetectAnomaliesUseCase useCase =
                new DetectAnomaliesUseCase(
                        transactionRepository,
                        anomalyRepository,
                        eventPublisher,
                        metricsPublisher,
                        List.of(alwaysMatch));

        List<Anomaly> result = useCase.detect(TX_ID);

        assertThat(result).hasSize(1);
        verify(anomalyRepository).save(any(Anomaly.class));
        verify(eventPublisher).publishAll(any());
        verify(metricsPublisher).incrementAnomaliesDetected(AnomalyType.AMOUNT, Severity.HIGH);
    }

    @Test
    void should_throw_when_transaction_not_found() {
        when(transactionRepository.findById(TX_ID)).thenReturn(Optional.empty());
        DetectAnomaliesUseCase useCase =
                new DetectAnomaliesUseCase(
                        transactionRepository,
                        anomalyRepository,
                        eventPublisher,
                        metricsPublisher,
                        List.of());

        assertThatThrownBy(() -> useCase.detect(TX_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tx_001");
    }
}
