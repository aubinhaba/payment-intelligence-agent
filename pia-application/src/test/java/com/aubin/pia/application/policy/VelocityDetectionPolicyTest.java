package com.aubin.pia.application.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class VelocityDetectionPolicyTest {

    @Mock TransactionRepository transactionRepository;

    private static final String CARD_HASH = "card_hash_velocity";
    private static final int WINDOW_HOURS = 1;
    private static final int HIGH_COUNT = 5;
    private static final int CRITICAL_COUNT = 10;

    private VelocityDetectionPolicy policy;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        policy =
                new VelocityDetectionPolicy(
                        transactionRepository, WINDOW_HOURS, HIGH_COUNT, CRITICAL_COUNT);
        transaction =
                Transaction.reconstitute(
                        new TransactionId("tx_velocity_test"),
                        Amount.of(new BigDecimal("50.00"), "EUR"),
                        new CardReference(CARD_HASH, "4242"),
                        new Merchant("m_001", "5812", "FR"),
                        Instant.now(),
                        TransactionStatus.AUTHORIZED);
    }

    @Test
    void should_return_empty_when_below_threshold() {
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS))
                .thenReturn(transactions(4));

        Optional<Anomaly> result = policy.detect(transaction);

        assertThat(result).isEmpty();
    }

    @Test
    void should_detect_high_anomaly_at_high_count() {
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS))
                .thenReturn(transactions(HIGH_COUNT));

        Optional<Anomaly> result = policy.detect(transaction);

        assertThat(result).isPresent();
        assertThat(result.get().type()).isEqualTo(AnomalyType.VELOCITY);
        assertThat(result.get().severity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void should_detect_critical_anomaly_at_critical_count() {
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS))
                .thenReturn(transactions(CRITICAL_COUNT));

        Optional<Anomaly> result = policy.detect(transaction);

        assertThat(result).isPresent();
        assertThat(result.get().type()).isEqualTo(AnomalyType.VELOCITY);
        assertThat(result.get().severity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void should_return_empty_when_no_recent_transactions() {
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS))
                .thenReturn(Collections.emptyList());

        Optional<Anomaly> result = policy.detect(transaction);

        assertThat(result).isEmpty();
    }

    @Test
    void should_include_count_in_description() {
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS))
                .thenReturn(transactions(HIGH_COUNT));

        Optional<Anomaly> result = policy.detect(transaction);

        assertThat(result).isPresent();
        assertThat(result.get().description()).contains(String.valueOf(HIGH_COUNT));
    }

    private static List<Transaction> transactions(int count) {
        return Collections.nCopies(
                count,
                Transaction.reconstitute(
                        new TransactionId("tx_hist"),
                        Amount.of(new BigDecimal("50.00"), "EUR"),
                        new CardReference(CARD_HASH, "4242"),
                        new Merchant("m_001", "5812", "FR"),
                        Instant.now(),
                        TransactionStatus.AUTHORIZED));
    }
}
