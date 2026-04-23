package com.aubin.pia.infrastructure.persistence.dynamodb;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.persistence.dynamodb.entity.AnomalyEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@Repository
public class DynamoDbAnomalyRepository implements AnomalyRepository {

    static final String TX_PK_PREFIX = "TX#";
    static final String SK_ANOMALY_PREFIX = "ANOMALY#";
    static final String GSI2_PK_PREFIX = "ANOMALY#";
    static final String GSI2_NAME = "gsi2";

    private final DynamoDbTable<AnomalyEntity> table;

    public DynamoDbAnomalyRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${pia.dynamodb.table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(AnomalyEntity.class));
    }

    @Override
    public void save(Anomaly anomaly) {
        table.putItem(toEntity(anomaly));
    }

    @Override
    public List<Anomaly> findByTransactionId(TransactionId transactionId) {
        QueryConditional condition =
                QueryConditional.sortBeginsWith(
                        Key.builder()
                                .partitionValue(TX_PK_PREFIX + transactionId.value())
                                .sortValue(SK_ANOMALY_PREFIX)
                                .build());
        return table.query(condition).stream()
                .flatMap(page -> page.items().stream())
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Anomaly> findSimilar(AnomalyType type, int limit) {
        DynamoDbIndex<AnomalyEntity> gsi2 = table.index(GSI2_NAME);
        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(GSI2_PK_PREFIX + type.name()).build());
        return gsi2
                .query(
                        QueryEnhancedRequest.builder()
                                .queryConditional(condition)
                                .limit(limit)
                                .scanIndexForward(false)
                                .build())
                .stream()
                .flatMap(page -> page.items().stream())
                .limit(limit)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AnomalyEntity toEntity(Anomaly anomaly) {
        AnomalyEntity e = new AnomalyEntity();
        e.setPk(TX_PK_PREFIX + anomaly.transactionId().value());
        e.setSk(SK_ANOMALY_PREFIX + anomaly.id().value());
        e.setGsi2Pk(GSI2_PK_PREFIX + anomaly.type().name());
        e.setGsi2Sk(anomaly.detectedAt().toString());
        e.setAnomalyId(anomaly.id().value());
        e.setTxId(anomaly.transactionId().value());
        e.setType(anomaly.type().name());
        e.setSeverity(anomaly.severity().name());
        e.setDescription(anomaly.description());
        e.setDetectedAt(anomaly.detectedAt().toString());
        return e;
    }

    private Anomaly toDomain(AnomalyEntity e) {
        return Anomaly.reconstitute(
                new AnomalyId(e.getAnomalyId()),
                new TransactionId(e.getTxId()),
                AnomalyType.valueOf(e.getType()),
                Severity.valueOf(e.getSeverity()),
                e.getDescription(),
                Instant.parse(e.getDetectedAt()));
    }
}
