package com.aubin.pia.infrastructure.persistence.dynamodb;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aubin.pia.infrastructure.persistence.dynamodb.entity.OutboxEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

/**
 * Polls the DynamoDB outbox (GSI4) every 5 seconds and processes unprocessed domain events.
 *
 * <p>Phase 2: logs and deletes. Phase 4+: routes events to downstream consumers via SQS/SNS.
 */
@Component
public class OutboxScheduledPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduledPublisher.class);
    private static final int BATCH_SIZE = 25;

    private final DynamoDbTable<OutboxEntity> table;
    private final DynamoDbIndex<OutboxEntity> gsi4;

    public OutboxScheduledPublisher(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${pia.dynamodb.table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(OutboxEntity.class));
        this.gsi4 = this.table.index("gsi4");
    }

    @Scheduled(fixedDelayString = "${pia.outbox.poll-interval-ms:5000}")
    public void processOutbox() {
        QueryConditional condition =
                QueryConditional.keyEqualTo(
                        Key.builder()
                                .partitionValue(OutboxEventPublisher.GSI4_PK_UNPROCESSED)
                                .build());

        List<OutboxEntity> batch = new ArrayList<>();
        gsi4
                .query(
                        QueryEnhancedRequest.builder()
                                .queryConditional(condition)
                                .limit(BATCH_SIZE)
                                .build())
                .stream()
                .flatMap(page -> page.items().stream())
                .limit(BATCH_SIZE)
                .forEach(batch::add);

        if (batch.isEmpty()) {
            return;
        }

        log.debug("Processing {} outbox event(s)", batch.size());
        batch.forEach(
                entity -> {
                    log.info(
                            "outbox.processed eventId={} type={}",
                            entity.getEventId(),
                            entity.getEventType());
                    table.deleteItem(
                            Key.builder()
                                    .partitionValue(entity.getPk())
                                    .sortValue(entity.getSk())
                                    .build());
                });
    }
}
