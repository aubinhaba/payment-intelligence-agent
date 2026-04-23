package com.aubin.pia.infrastructure.persistence.dynamodb.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * DynamoDB entity for Anomaly. Single-table design:
 *
 * <pre>
 * PK = TX#${txId}        SK = ANOMALY#${anomalyId}
 * GSI2: gsi2Pk = ANOMALY#${type}  gsi2Sk = ${detectedAt} (for findSimilar)
 * </pre>
 */
@DynamoDbBean
public class AnomalyEntity {

    private String pk;
    private String sk;
    private String gsi2Pk;
    private String gsi2Sk;

    private String anomalyId;
    private String txId;
    private String type;
    private String severity;
    private String description;
    private String detectedAt;

    public AnomalyEntity() {}

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

    @DynamoDbSecondaryPartitionKey(indexNames = {"gsi2"})
    public String getGsi2Pk() {
        return gsi2Pk;
    }

    public void setGsi2Pk(String gsi2Pk) {
        this.gsi2Pk = gsi2Pk;
    }

    @DynamoDbSecondarySortKey(indexNames = {"gsi2"})
    public String getGsi2Sk() {
        return gsi2Sk;
    }

    public void setGsi2Sk(String gsi2Sk) {
        this.gsi2Sk = gsi2Sk;
    }

    public String getAnomalyId() {
        return anomalyId;
    }

    public void setAnomalyId(String anomalyId) {
        this.anomalyId = anomalyId;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(String detectedAt) {
        this.detectedAt = detectedAt;
    }
}
