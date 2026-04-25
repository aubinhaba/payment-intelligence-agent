package com.aubin.pia.infrastructure.persistence.dynamodb;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;
import com.aubin.pia.infrastructure.persistence.dynamodb.entity.TransactionEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class DynamoDbTransactionRepository implements TransactionRepository {

    static final String PK_PREFIX = "TX#";
    static final String SK_METADATA = "METADATA";
    static final String GSI1_PK_PREFIX = "CARD#";
    static final String GSI1_NAME = "gsi1";

    private final DynamoDbTable<TransactionEntity> table;

    public DynamoDbTransactionRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${pia.dynamodb.table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(TransactionEntity.class));
    }

    @Override
    public void save(Transaction tx) {
        table.putItem(toEntity(tx));
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        Key key =
                Key.builder().partitionValue(PK_PREFIX + id.value()).sortValue(SK_METADATA).build();
        return Optional.ofNullable(table.getItem(key)).map(this::toDomain);
    }

    @Override
    public List<Transaction> findByCardReference(String cardHash, int windowHours) {
        DynamoDbIndex<TransactionEntity> gsi1 = table.index(GSI1_NAME);
        Instant cutoff = Instant.now().minus(windowHours, ChronoUnit.HOURS);
        QueryConditional condition =
                QueryConditional.sortGreaterThanOrEqualTo(
                        Key.builder()
                                .partitionValue(GSI1_PK_PREFIX + cardHash)
                                .sortValue(cutoff.toString())
                                .build());
        return gsi1
                .query(QueryEnhancedRequest.builder().queryConditional(condition).build())
                .stream()
                .flatMap(page -> page.items().stream())
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByMerchantId(String merchantId, int windowHours) {
        Instant cutoff = Instant.now().minus(windowHours, ChronoUnit.HOURS);
        Expression filter =
                Expression.builder()
                        .expression("merchantId = :merchantId AND occurredAt >= :cutoff")
                        .expressionValues(
                                java.util.Map.of(
                                        ":merchantId", AttributeValue.fromS(merchantId),
                                        ":cutoff", AttributeValue.fromS(cutoff.toString())))
                        .build();
        return table.scan(ScanEnhancedRequest.builder().filterExpression(filter).build()).stream()
                .flatMap(page -> page.items().stream())
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private TransactionEntity toEntity(Transaction tx) {
        TransactionEntity e = new TransactionEntity();
        e.setPk(PK_PREFIX + tx.id().value());
        e.setSk(SK_METADATA);
        e.setGsi1Pk(GSI1_PK_PREFIX + tx.cardReference().hash());
        e.setGsi1Sk(tx.occurredAt().toString());
        e.setTxId(tx.id().value());
        e.setAmountValue(tx.amount().value().toPlainString());
        e.setCurrency(tx.amount().currency().getCurrencyCode());
        e.setCardHash(tx.cardReference().hash());
        e.setCardLast4(tx.cardReference().last4());
        e.setMerchantId(tx.merchant().id());
        e.setMerchantMcc(tx.merchant().mcc());
        e.setMerchantCountry(tx.merchant().country());
        e.setOccurredAt(tx.occurredAt().toString());
        e.setStatus(tx.status().name());
        return e;
    }

    private Transaction toDomain(TransactionEntity e) {
        return Transaction.reconstitute(
                new TransactionId(e.getTxId()),
                new Amount(
                        new BigDecimal(e.getAmountValue()), Currency.getInstance(e.getCurrency())),
                new CardReference(e.getCardHash(), e.getCardLast4()),
                new Merchant(e.getMerchantId(), e.getMerchantMcc(), e.getMerchantCountry()),
                Instant.parse(e.getOccurredAt()),
                TransactionStatus.valueOf(e.getStatus()));
    }
}
