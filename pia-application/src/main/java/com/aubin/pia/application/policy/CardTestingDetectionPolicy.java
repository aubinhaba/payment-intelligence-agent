package com.aubin.pia.application.policy;

import java.math.BigDecimal;
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
 * Detects card-testing behaviour: a card submitting many micro-transactions to verify it is live.
 *
 * <p>Counts transactions for the same card hash whose amount is at or below {@code maxMicroAmount}
 * within the configured time window.
 */
public class CardTestingDetectionPolicy implements DetectionPolicy {

    private final TransactionRepository transactionRepository;
    private final int windowHours;
    private final int minCount;
    private final int highCount;
    private final BigDecimal maxMicroAmount;

    public CardTestingDetectionPolicy(
            TransactionRepository transactionRepository,
            int windowHours,
            int minCount,
            int highCount,
            BigDecimal maxMicroAmount) {
        this.transactionRepository = transactionRepository;
        this.windowHours = windowHours;
        this.minCount = minCount;
        this.highCount = highCount;
        this.maxMicroAmount = maxMicroAmount;
    }

    @Override
    public Optional<Anomaly> detect(Transaction transaction) {
        String cardHash = transaction.cardReference().hash();
        List<Transaction> recent = transactionRepository.findByCardReference(cardHash, windowHours);

        long microCount =
                recent.stream()
                        .filter(tx -> tx.amount().value().compareTo(maxMicroAmount) <= 0)
                        .count();

        Severity severity;
        if (microCount >= highCount) {
            severity = Severity.HIGH;
        } else if (microCount >= minCount) {
            severity = Severity.MEDIUM;
        } else {
            return Optional.empty();
        }

        return Optional.of(
                Anomaly.detect(
                        new AnomalyId(UUID.randomUUID().toString()),
                        transaction.id(),
                        AnomalyType.CARD_TESTING,
                        severity,
                        String.format(
                                "Card testing suspected: %d micro-transactions (≤%.2f) in the last"
                                        + " %d hour(s)",
                                microCount, maxMicroAmount, windowHours)));
    }
}
