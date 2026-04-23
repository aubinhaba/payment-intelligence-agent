package com.aubin.pia.infrastructure.persistence.dynamodb.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * DynamoDB entity for the Outbox pattern. Single-table design:
 *
 * <pre>
 * PK = OUTBOX#${eventId}          SK = ${createdAt}
 * GSI4: gsi4Pk = OUTBOX#unprocessed  gsi4Sk = ${createdAt}
 * </pre>
 *
 * <p>Records are deleted (not updated) once processed by {@code OutboxScheduledPublisher}.
 */
@DynamoDbBean
public class OutboxEntity {

    private String pk;
    private String sk;
    private String gsi4Pk;
    private String gsi4Sk;

    private String eventId;
    private String eventType;
    private String payload;
    private String createdAt;

    public OutboxEntity() {}

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"gsi4"})
    public String getGsi4Pk() {
        return gsi4Pk;
    }

    public void setGsi4Pk(String gsi4Pk) {
        this.gsi4Pk = gsi4Pk;
    }

    @DynamoDbSecondarySortKey(indexNames = {"gsi4"})
    public String getGsi4Sk() {
        return gsi4Sk;
    }

    public void setGsi4Sk(String gsi4Sk) {
        this.gsi4Sk = gsi4Sk;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
