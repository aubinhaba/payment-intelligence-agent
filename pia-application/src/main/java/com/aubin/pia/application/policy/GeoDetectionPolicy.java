package com.aubin.pia.application.policy;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.DetectionPolicy;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Transaction;

/**
 * Flags transactions originating from high-risk merchant countries.
 *
 * <p>Severity escalates to HIGH when the amount also exceeds {@code highAmountThreshold}.
 */
public class GeoDetectionPolicy implements DetectionPolicy {

    /** Amount above which a high-risk-country transaction is escalated to HIGH. */
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = BigDecimal.valueOf(500);

    private final Set<String> highRiskCountries;

    public GeoDetectionPolicy(Set<String> highRiskCountries) {
        this.highRiskCountries = Set.copyOf(highRiskCountries);
    }

    @Override
    public Optional<Anomaly> detect(Transaction transaction) {
        String country = transaction.merchant().country();
        if (!highRiskCountries.contains(country)) {
            return Optional.empty();
        }

        BigDecimal amount = transaction.amount().value();
        Severity severity =
                amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0 ? Severity.HIGH : Severity.MEDIUM;

        return Optional.of(
                Anomaly.detect(
                        new AnomalyId(UUID.randomUUID().toString()),
                        transaction.id(),
                        AnomalyType.GEO,
                        severity,
                        String.format(
                                "Transaction at merchant %s in high-risk country %s (amount %.2f"
                                        + " %s)",
                                transaction.merchant().id(),
                                country,
                                amount,
                                transaction.amount().currency().getCurrencyCode())));
    }
}
