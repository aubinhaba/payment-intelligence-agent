package com.aubin.pia.application.policy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.DetectionPolicy;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Transaction;

/**
 * Flags cards that have been used too many times within a short time window.
 *
 * <p>Counts transactions already persisted for the same card hash (including the current one),
 * using {@link TransactionRepository#findByCardReference}.
 */
public class VelocityDetectionPolicy implements DetectionPolicy {

    private final TransactionRepository transactionRepository;
    private final int windowHours;
    private final int highCount;
    private final int criticalCount;

    public VelocityDetectionPolicy(
            TransactionRepository transactionRepository,
            int windowHours,
            int highCount,
            int criticalCount) {
        this.transactionRepository = transactionRepository;
        this.windowHours = windowHours;
        this.highCount = highCount;
        this.criticalCount = criticalCount;
    }

    @Override
    public Optional<Anomaly> detect(Transaction transaction) {
        String cardHash = transaction.cardReference().hash();
        List<Transaction> recent = transactionRepository.findByCardReference(cardHash, windowHours);
        int count = recent.size();

        Severity severity;
        if (count >= criticalCount) {
            severity = Severity.CRITICAL;
        } else if (count >= highCount) {
            severity = Severity.HIGH;
        } else {
            return Optional.empty();
        }

        return Optional.of(
                Anomaly.detect(
                        new AnomalyId(UUID.randomUUID().toString()),
                        transaction.id(),
                        AnomalyType.VELOCITY,
                        severity,
                        String.format(
                                "Card used %d times in the last %d hour(s) — %s velocity",
                                count, windowHours, severity.name().toLowerCase())));
    }
}
