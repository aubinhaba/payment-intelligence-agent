package com.aubin.pia.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.aubin.pia.application.policy.AmountDetectionPolicy;
import com.aubin.pia.application.policy.CardTestingDetectionPolicy;
import com.aubin.pia.application.policy.GeoDetectionPolicy;
import com.aubin.pia.application.policy.VelocityDetectionPolicy;
import com.aubin.pia.application.port.out.EventPublisher;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.usecase.DetectAnomaliesUseCase;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;
import com.aubin.pia.infrastructure.persistence.dynamodb.DynamoDbAnomalyRepository;
import com.aubin.pia.infrastructure.persistence.dynamodb.DynamoDbTransactionRepository;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * End-to-end integration test for {@link DetectAnomaliesUseCase} wired with real DynamoDB adapters
 * and all four detection policies against LocalStack.
 */
@Testcontainers
class DetectAnomaliesUseCaseIT {

    @Container
    static final LocalStackContainer LOCAL_STACK =
            LocalStackTestSupport.newContainer(Service.DYNAMODB);

    private static DynamoDbTransactionRepository transactionRepository;
    private static DynamoDbAnomalyRepository anomalyRepository;
    private static DetectAnomaliesUseCase useCase;

    @BeforeAll
    static void setUpOnce() {
        DynamoDbClient client = LocalStackTestSupport.dynamoDbClient(LOCAL_STACK);
        LocalStackTestSupport.createPiaTable(client);

        DynamoDbEnhancedClient enhanced = LocalStackTestSupport.enhancedClient(LOCAL_STACK);
        transactionRepository =
                new DynamoDbTransactionRepository(enhanced, LocalStackTestSupport.TABLE_NAME);
        anomalyRepository =
                new DynamoDbAnomalyRepository(enhanced, LocalStackTestSupport.TABLE_NAME);

        // Low thresholds to make the simulator scenarios trigger easily in tests
        AmountDetectionPolicy amountPolicy =
                new AmountDetectionPolicy(new BigDecimal("500.00"), new BigDecimal("1000.00"));
        GeoDetectionPolicy geoPolicy =
                new GeoDetectionPolicy(Set.of("AE", "MT", "SG", "HK", "RU", "NG"));
        VelocityDetectionPolicy velocityPolicy =
                new VelocityDetectionPolicy(transactionRepository, 1, 3, 6);
        CardTestingDetectionPolicy cardTestingPolicy =
                new CardTestingDetectionPolicy(
                        transactionRepository, 1, 2, 4, new BigDecimal("2.00"));

        useCase =
                new DetectAnomaliesUseCase(
                        transactionRepository,
                        anomalyRepository,
                        mock(EventPublisher.class),
                        mock(MetricsPublisher.class),
                        List.of(amountPolicy, geoPolicy, velocityPolicy, cardTestingPolicy));
    }

    @Test
    void normal_transaction_produces_no_anomalies() {
        Transaction tx =
                saveTransaction(
                        uniqueId(),
                        "card_normal_" + UUID.randomUUID(),
                        new BigDecimal("120.00"),
                        "FR");

        List<Anomaly> anomalies = useCase.detect(tx.id());

        assertThat(anomalies).isEmpty();
    }

    @Test
    void high_value_transaction_triggers_amount_anomaly() {
        Transaction tx =
                saveTransaction(
                        uniqueId(),
                        "card_amount_" + UUID.randomUUID(),
                        new BigDecimal("6000.00"),
                        "FR");

        List<Anomaly> anomalies = useCase.detect(tx.id());

        assertThat(anomalies)
                .anySatisfy(
                        a -> {
                            assertThat(a.type()).isEqualTo(AnomalyType.AMOUNT);
                            assertThat(a.severity()).isEqualTo(Severity.HIGH);
                        });
    }

    @Test
    void critical_value_transaction_triggers_critical_amount_anomaly() {
        Transaction tx =
                saveTransaction(
                        uniqueId(),
                        "card_critical_" + UUID.randomUUID(),
                        new BigDecimal("1500.00"),
                        "FR");

        List<Anomaly> anomalies = useCase.detect(tx.id());

        assertThat(anomalies)
                .anySatisfy(
                        a -> {
                            assertThat(a.type()).isEqualTo(AnomalyType.AMOUNT);
                            assertThat(a.severity()).isEqualTo(Severity.CRITICAL);
                        });
    }

    @Test
    void high_risk_country_transaction_triggers_geo_anomaly() {
        Transaction tx =
                saveTransaction(
                        uniqueId(),
                        "card_geo_" + UUID.randomUUID(),
                        new BigDecimal("200.00"),
                        "AE");

        List<Anomaly> anomalies = useCase.detect(tx.id());

        assertThat(anomalies).anySatisfy(a -> assertThat(a.type()).isEqualTo(AnomalyType.GEO));
    }

    @Test
    void velocity_burst_triggers_velocity_anomaly() {
        String cardHash = "card_velocity_" + UUID.randomUUID();
        // Pre-save 3 transactions to hit the HIGH threshold (highCount=3)
        saveTransaction(uniqueId(), cardHash, new BigDecimal("50.00"), "FR");
        saveTransaction(uniqueId(), cardHash, new BigDecimal("50.00"), "FR");
        Transaction tx = saveTransaction(uniqueId(), cardHash, new BigDecimal("50.00"), "FR");

        List<Anomaly> anomalies = useCase.detect(tx.id());

        assertThat(anomalies)
                .anySatisfy(
                        a -> {
                            assertThat(a.type()).isEqualTo(AnomalyType.VELOCITY);
                            assertThat(a.severity()).isEqualTo(Severity.HIGH);
                        });
    }

    @Test
    void card_testing_micro_transactions_trigger_card_testing_anomaly() {
        String cardHash = "card_testing_" + UUID.randomUUID();
        // Pre-save 2 micro-transactions to hit minCount=2
        saveTransaction(uniqueId(), cardHash, new BigDecimal("0.99"), "FR");
        Transaction tx = saveTransaction(uniqueId(), cardHash, new BigDecimal("1.50"), "FR");

        List<Anomaly> anomalies = useCase.detect(tx.id());

        assertThat(anomalies)
                .anySatisfy(
                        a -> {
                            assertThat(a.type()).isEqualTo(AnomalyType.CARD_TESTING);
                            assertThat(a.severity()).isEqualTo(Severity.MEDIUM);
                        });
    }

    @Test
    void anomalies_are_persisted_and_linked_to_transaction() {
        Transaction tx =
                saveTransaction(
                        uniqueId(),
                        "card_persist_" + UUID.randomUUID(),
                        new BigDecimal("800.00"),
                        "MT");

        List<Anomaly> detected = useCase.detect(tx.id());

        // Reload from DynamoDB and verify persistence
        List<Anomaly> persisted = anomalyRepository.findByTransactionId(tx.id());
        assertThat(persisted).hasSameSizeAs(detected);
        assertThat(persisted).allMatch(a -> a.transactionId().equals(tx.id()));
    }

    // --- helpers ---

    private static TransactionId uniqueId() {
        return new TransactionId(
                "tx_it_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
    }

    private static Transaction saveTransaction(
            TransactionId id, String cardHash, BigDecimal amount, String country) {
        Transaction tx =
                Transaction.reconstitute(
                        id,
                        Amount.of(amount, "EUR"),
                        new CardReference(cardHash, "4242"),
                        new Merchant("m_it_test", "5812", country),
                        Instant.now(),
                        TransactionStatus.AUTHORIZED);
        transactionRepository.save(tx);
        return tx;
    }
}
