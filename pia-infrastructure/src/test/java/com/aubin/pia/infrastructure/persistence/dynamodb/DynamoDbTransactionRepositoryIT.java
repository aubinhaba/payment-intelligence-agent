package com.aubin.pia.infrastructure.persistence.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;
import com.aubin.pia.infrastructure.LocalStackTestSupport;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Testcontainers
class DynamoDbTransactionRepositoryIT {

    @Container
    static final LocalStackContainer LOCAL_STACK =
            LocalStackTestSupport.newContainer(Service.DYNAMODB);

    private DynamoDbTransactionRepository repository;

    @BeforeEach
    void setUp() {
        DynamoDbClient client = LocalStackTestSupport.dynamoDbClient(LOCAL_STACK);
        LocalStackTestSupport.createPiaTable(client);
        DynamoDbEnhancedClient enhancedClient = LocalStackTestSupport.enhancedClient(LOCAL_STACK);
        repository =
                new DynamoDbTransactionRepository(enhancedClient, LocalStackTestSupport.TABLE_NAME);
    }

    @Test
    void save_then_findById_roundTrip() {
        Transaction tx = buildTransaction("tx-001", "card-hash-001", Instant.now());

        repository.save(tx);

        Optional<Transaction> found = repository.findById(new TransactionId("tx-001"));
        assertThat(found).isPresent();
        assertThat(found.get().id().value()).isEqualTo("tx-001");
        assertThat(found.get().amount().value()).isEqualByComparingTo("120.50");
        assertThat(found.get().amount().currency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(found.get().cardReference().hash()).isEqualTo("card-hash-001");
        assertThat(found.get().cardReference().last4()).isEqualTo("4242");
        assertThat(found.get().status()).isEqualTo(TransactionStatus.AUTHORIZED);
    }

    @Test
    void findById_missing_returns_empty() {
        Optional<Transaction> found = repository.findById(new TransactionId("tx-nonexistent"));
        assertThat(found).isEmpty();
    }

    @Test
    void findByCardReference_returns_transactions_in_window() {
        Instant now = Instant.now();
        Transaction recent = buildTransaction("tx-recent", "card-hash-002", now.minusSeconds(60));
        Transaction old =
                buildTransaction("tx-old", "card-hash-002", now.minus(25, ChronoUnit.HOURS));

        repository.save(recent);
        repository.save(old);

        List<Transaction> results = repository.findByCardReference("card-hash-002", 24);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id().value()).isEqualTo("tx-recent");
    }

    private static Transaction buildTransaction(String txId, String cardHash, Instant occurredAt) {
        return Transaction.create(
                new TransactionId(txId),
                new Amount(new BigDecimal("120.50"), java.util.Currency.getInstance("EUR")),
                new CardReference(cardHash, "4242"),
                new Merchant("m_test", "5812", "FR"),
                occurredAt,
                TransactionStatus.AUTHORIZED);
    }
}
