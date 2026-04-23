package com.aubin.pia.infrastructure.persistence.dynamodb;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.application.port.out.EventPublisher;
import com.aubin.pia.domain.shared.DomainEvent;
import com.aubin.pia.infrastructure.persistence.dynamodb.entity.OutboxEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Component
public class OutboxEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    static final String OUTBOX_PK_PREFIX = "OUTBOX#";
    static final String GSI4_PK_UNPROCESSED = "OUTBOX#unprocessed";

    private final DynamoDbTable<OutboxEntity> table;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${pia.dynamodb.table-name}") String tableName,
            ObjectMapper objectMapper) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(OutboxEntity.class));
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::writeToOutbox);
    }

    private void writeToOutbox(DomainEvent event) {
        String eventId = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            log.warn(
                    "Could not serialise event {}: {}",
                    event.getClass().getSimpleName(),
                    ex.getMessage());
            payload = "{}";
        }

        OutboxEntity entity = new OutboxEntity();
        entity.setPk(OUTBOX_PK_PREFIX + eventId);
        entity.setSk(createdAt);
        entity.setGsi4Pk(GSI4_PK_UNPROCESSED);
        entity.setGsi4Sk(createdAt);
        entity.setEventId(eventId);
        entity.setEventType(event.getClass().getSimpleName());
        entity.setPayload(payload);
        entity.setCreatedAt(createdAt);

        table.putItem(entity);
        log.debug(
                "Wrote outbox event eventId={} type={}", eventId, event.getClass().getSimpleName());
    }
}
