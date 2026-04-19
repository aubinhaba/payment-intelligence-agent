package com.aubin.pia.domain.anomaly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.aubin.pia.domain.shared.DomainEvent;
import com.aubin.pia.domain.transaction.TransactionId;

class AnomalyTest {

    @Test
    void should_detect_anomaly_with_correct_fields() {
        Anomaly anomaly =
                Anomaly.detect(
                        new AnomalyId("a_001"),
                        new TransactionId("tx_001"),
                        AnomalyType.VELOCITY,
                        Severity.HIGH,
                        "Card used 50 times in 5 minutes");

        assertThat(anomaly.id().value()).isEqualTo("a_001");
        assertThat(anomaly.type()).isEqualTo(AnomalyType.VELOCITY);
        assertThat(anomaly.severity()).isEqualTo(Severity.HIGH);
        assertThat(anomaly.detectedAt()).isNotNull();
    }

    @Test
    void should_raise_anomaly_detected_event() {
        Anomaly anomaly =
                Anomaly.detect(
                        new AnomalyId("a_001"),
                        new TransactionId("tx_001"),
                        AnomalyType.AMOUNT,
                        Severity.CRITICAL,
                        "Amount far above card average");

        List<DomainEvent> events = anomaly.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(AnomalyDetected.class);
        AnomalyDetected event = (AnomalyDetected) events.get(0);
        assertThat(event.type()).isEqualTo(AnomalyType.AMOUNT);
        assertThat(event.severity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void should_reject_blank_description() {
        assertThatThrownBy(
                        () ->
                                Anomaly.detect(
                                        new AnomalyId("a_001"),
                                        new TransactionId("tx_001"),
                                        AnomalyType.GEO,
                                        Severity.MEDIUM,
                                        "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("description");
    }

    @Test
    void should_reject_null_type() {
        assertThatThrownBy(
                        () ->
                                Anomaly.detect(
                                        new AnomalyId("a_001"),
                                        new TransactionId("tx_001"),
                                        null,
                                        Severity.LOW,
                                        "desc"))
                .isInstanceOf(NullPointerException.class);
    }
}
