package com.aubin.pia.infrastructure.persistence.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.LocalStackTestSupport;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Testcontainers
class DynamoDbAnomalyRepositoryIT {

    @Container
    static final LocalStackContainer LOCAL_STACK =
            LocalStackTestSupport.newContainer(Service.DYNAMODB);

    private DynamoDbAnomalyRepository repository;

    @BeforeEach
    void setUp() {
        DynamoDbClient client = LocalStackTestSupport.dynamoDbClient(LOCAL_STACK);
        LocalStackTestSupport.createPiaTable(client);
        DynamoDbEnhancedClient enhancedClient = LocalStackTestSupport.enhancedClient(LOCAL_STACK);
        repository =
                new DynamoDbAnomalyRepository(enhancedClient, LocalStackTestSupport.TABLE_NAME);
    }

    @Test
    void save_then_findByTransactionId_roundTrip() {
        Anomaly anomaly =
                Anomaly.detect(
                        new AnomalyId("an-001"),
                        new TransactionId("tx-001"),
                        AnomalyType.AMOUNT,
                        Severity.HIGH,
                        "Amount exceeds threshold");
        anomaly.pullDomainEvents();

        repository.save(anomaly);

        List<Anomaly> results = repository.findByTransactionId(new TransactionId("tx-001"));
        assertThat(results).hasSize(1);
        assertThat(results.get(0).id().value()).isEqualTo("an-001");
        assertThat(results.get(0).type()).isEqualTo(AnomalyType.AMOUNT);
        assertThat(results.get(0).severity()).isEqualTo(Severity.HIGH);
    }

    @Test
    void findByTransactionId_unknown_returns_empty() {
        List<Anomaly> results = repository.findByTransactionId(new TransactionId("tx-unknown"));
        assertThat(results).isEmpty();
    }

    @Test
    void findSimilar_returns_anomalies_of_same_type() {
        Anomaly a1 =
                Anomaly.detect(
                        new AnomalyId("an-v1"),
                        new TransactionId("tx-a"),
                        AnomalyType.VELOCITY,
                        Severity.MEDIUM,
                        "Velocity breach 1");
        Anomaly a2 =
                Anomaly.detect(
                        new AnomalyId("an-v2"),
                        new TransactionId("tx-b"),
                        AnomalyType.VELOCITY,
                        Severity.HIGH,
                        "Velocity breach 2");
        Anomaly a3 =
                Anomaly.detect(
                        new AnomalyId("an-a1"),
                        new TransactionId("tx-c"),
                        AnomalyType.AMOUNT,
                        Severity.LOW,
                        "Amount anomaly");
        a1.pullDomainEvents();
        a2.pullDomainEvents();
        a3.pullDomainEvents();

        repository.save(a1);
        repository.save(a2);
        repository.save(a3);

        List<Anomaly> similar = repository.findSimilar(AnomalyType.VELOCITY, 10);
        assertThat(similar).hasSize(2);
        assertThat(similar).allMatch(a -> a.type() == AnomalyType.VELOCITY);
    }
}
