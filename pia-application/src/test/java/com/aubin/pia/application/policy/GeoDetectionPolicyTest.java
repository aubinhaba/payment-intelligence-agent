package com.aubin.pia.application.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;

class GeoDetectionPolicyTest {

    private GeoDetectionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new GeoDetectionPolicy(Set.of("AE", "MT", "SG", "HK", "RU", "NG"));
    }

    @Test
    void should_return_empty_for_safe_country() {
        Optional<Anomaly> result = policy.detect(transaction("FR", "120.00"));

        assertThat(result).isEmpty();
    }

    @Test
    void should_detect_medium_anomaly_for_high_risk_country_low_amount() {
        Optional<Anomaly> result = policy.detect(transaction("AE", "100.00"));

        assertThat(result).isPresent();
        assertThat(result.get().type()).isEqualTo(AnomalyType.GEO);
        assertThat(result.get().severity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void should_detect_high_anomaly_for_high_risk_country_above_500() {
        Optional<Anomaly> result = policy.detect(transaction("SG", "500.00"));

        assertThat(result).isPresent();
        assertThat(result.get().type()).isEqualTo(AnomalyType.GEO);
        assertThat(result.get().severity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void should_detect_anomaly_for_all_configured_high_risk_countries() {
        for (String country : Set.of("AE", "MT", "SG", "HK", "RU", "NG")) {
            assertThat(policy.detect(transaction(country, "50.00")))
                    .as("Expected anomaly for country %s", country)
                    .isPresent();
        }
    }

    @Test
    void should_include_merchant_country_in_description() {
        Optional<Anomaly> result = policy.detect(transaction("MT", "200.00"));

        assertThat(result).isPresent();
        assertThat(result.get().description()).contains("MT");
    }

    private static Transaction transaction(String country, String amount) {
        return Transaction.reconstitute(
                new TransactionId("tx_geo_test"),
                Amount.of(new BigDecimal(amount), "EUR"),
                new CardReference("hash_001", "4242"),
                new Merchant("m_001", "5944", country),
                Instant.now(),
                TransactionStatus.AUTHORIZED);
    }
}
