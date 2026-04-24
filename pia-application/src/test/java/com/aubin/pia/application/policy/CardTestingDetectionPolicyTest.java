package com.aubin.pia.application.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

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
class CardTestingDetectionPolicyTest {

    @Mock TransactionRepository transactionRepository;

    private static final String CARD_HASH = "card_hash_testing";
    private static final BigDecimal MAX_MICRO = new BigDecimal("2.00");
    private static final int WINDOW_HOURS = 1;
    private static final int MIN_COUNT = 3;
    private static final int HIGH_COUNT = 5;

    private CardTestingDetectionPolicy policy;
    private Transaction currentTransaction;

    @BeforeEach
    void setUp() {
        policy =
                new CardTestingDetectionPolicy(
                        transactionRepository, WINDOW_HOURS, MIN_COUNT, HIGH_COUNT, MAX_MICRO);
        currentTransaction = transaction("1.00");
    }

    @Test
    void should_return_empty_when_too_few_micro_transactions() {
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS))
                .thenReturn(microTransactions(2));

        Optional<Anomaly> result = policy.detect(currentTransaction);

        assertThat(result).isEmpty();
    }

    @Test
    void should_detect_medium_anomaly_at_min_count() {
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS))
                .thenReturn(microTransactions(MIN_COUNT));

        Optional<Anomaly> result = policy.detect(currentTransaction);

        assertThat(result).isPresent();
        assertThat(result.get().type()).isEqualTo(AnomalyType.CARD_TESTING);
        assertThat(result.get().severity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void should_detect_high_anomaly_at_high_count() {
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS))
                .thenReturn(microTransactions(HIGH_COUNT));

        Optional<Anomaly> result = policy.detect(currentTransaction);

        assertThat(result).isPresent();
        assertThat(result.get().type()).isEqualTo(AnomalyType.CARD_TESTING);
        assertThat(result.get().severity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void should_not_count_normal_amount_transactions() {
        // 4 normal-amount transactions + 2 micro — total 6 transactions, but only 2 micro
        List<Transaction> mixed = new java.util.ArrayList<>(microTransactions(2));
        mixed.addAll(normalTransactions(4));
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS)).thenReturn(mixed);

        Optional<Anomaly> result = policy.detect(currentTransaction);

        assertThat(result).isEmpty();
    }

    @Test
    void should_count_exact_micro_threshold_amount() {
        // Transaction at exactly MAX_MICRO must be counted as micro
        List<Transaction> atThreshold =
                List.of(transaction("2.00"), transaction("2.00"), transaction("2.00"));
        when(transactionRepository.findByCardReference(CARD_HASH, WINDOW_HOURS))
                .thenReturn(atThreshold);

        Optional<Anomaly> result = policy.detect(currentTransaction);

        assertThat(result).isPresent();
        assertThat(result.get().severity()).isEqualTo(Severity.MEDIUM);
    }

    private List<Transaction> microTransactions(int count) {
        return IntStream.range(0, count).mapToObj(i -> transaction("0.99")).toList();
    }

    private List<Transaction> normalTransactions(int count) {
        return IntStream.range(0, count).mapToObj(i -> transaction("50.00")).toList();
    }

    private Transaction transaction(String amount) {
        return Transaction.reconstitute(
                new TransactionId("tx_ct_" + amount.replace(".", "_")),
                Amount.of(new BigDecimal(amount), "EUR"),
                new CardReference(CARD_HASH, "4242"),
                new Merchant("m_001", "5812", "FR"),
                Instant.now(),
                TransactionStatus.AUTHORIZED);
    }
}
