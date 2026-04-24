package com.aubin.pia.application.policy;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.DetectionPolicy;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Transaction;

/**
 * Flags transactions whose amount exceeds configured thresholds.
 *
 * <p>Thresholds are injected at construction time from {@code pia.detection.amount.*} config.
 */
public class AmountDetectionPolicy implements DetectionPolicy {

    private final BigDecimal highThreshold;
    private final BigDecimal criticalThreshold;

    public AmountDetectionPolicy(BigDecimal highThreshold, BigDecimal criticalThreshold) {
        this.highThreshold = highThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public Optional<Anomaly> detect(Transaction transaction) {
        BigDecimal value = transaction.amount().value();

        Severity severity;
        if (value.compareTo(criticalThreshold) >= 0) {
            severity = Severity.CRITICAL;
        } else if (value.compareTo(highThreshold) >= 0) {
            severity = Severity.HIGH;
        } else {
            return Optional.empty();
        }

        return Optional.of(
                Anomaly.detect(
                        new AnomalyId(UUID.randomUUID().toString()),
                        transaction.id(),
                        AnomalyType.AMOUNT,
                        severity,
                        String.format(
                                "Transaction amount %.2f %s exceeds %s threshold",
                                value,
                                transaction.amount().currency().getCurrencyCode(),
                                severity.name().toLowerCase())));
    }
}
