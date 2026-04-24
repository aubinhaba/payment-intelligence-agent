package com.aubin.pia.application.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

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

class AmountDetectionPolicyTest {

    private static final BigDecimal HIGH = new BigDecimal("5000.00");
    private static final BigDecimal CRITICAL = new BigDecimal("10000.00");

    private AmountDetectionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AmountDetectionPolicy(HIGH, CRITICAL);
    }

    @Test
    void should_return_empty_for_normal_amount() {
        Optional<Anomaly> result = policy.detect(transaction("4999.99"));

        assertThat(result).isEmpty();
    }

    @Test
    void should_detect_high_anomaly_at_high_threshold() {
        Optional<Anomaly> result = policy.detect(transaction("5000.00"));

        assertThat(result).isPresent();
        assertThat(result.get().type()).isEqualTo(AnomalyType.AMOUNT);
        assertThat(result.get().severity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void should_detect_high_anomaly_above_high_threshold() {
        Optional<Anomaly> result = policy.detect(transaction("7500.00"));

        assertThat(result).isPresent();
        assertThat(result.get().severity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void should_detect_critical_anomaly_at_critical_threshold() {
        Optional<Anomaly> result = policy.detect(transaction("10000.00"));

        assertThat(result).isPresent();
        assertThat(result.get().type()).isEqualTo(AnomalyType.AMOUNT);
        assertThat(result.get().severity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void should_link_anomaly_to_transaction_id() {
        Transaction tx = transaction("6000.00");
        Optional<Anomaly> result = policy.detect(tx);

        assertThat(result).isPresent();
        assertThat(result.get().transactionId()).isEqualTo(tx.id());
    }

    private static Transaction transaction(String amount) {
        return Transaction.reconstitute(
                new TransactionId("tx_amount_test"),
                Amount.of(new BigDecimal(amount), "EUR"),
                new CardReference("hash_001", "4242"),
                new Merchant("m_001", "5812", "FR"),
                Instant.now(),
                TransactionStatus.AUTHORIZED);
    }
}
