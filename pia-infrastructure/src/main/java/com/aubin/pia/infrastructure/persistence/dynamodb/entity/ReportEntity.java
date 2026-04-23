package com.aubin.pia.infrastructure.persistence.dynamodb.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * DynamoDB entity for Report metadata. Single-table design:
 *
 * <pre>
 * PK = REPORT#${reportId}   SK = METADATA
 * GSI3: gsi3Pk = REPORT#daily  gsi3Sk = ${date} (for daily listing)
 * </pre>
 *
 * <p>Full report body is stored in S3; only metadata and summary are kept here.
 */
@DynamoDbBean
public class ReportEntity {

    private String pk;
    private String sk;
    private String gsi3Pk;
    private String gsi3Sk;

    private String reportId;
    private String txId;
    private String summary;
    private String markdownBody;
    private String s3Key;
    private String generatedAt;

    public ReportEntity() {}

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

    @DynamoDbSecondaryPartitionKey(indexNames = {"gsi3"})
    public String getGsi3Pk() {
        return gsi3Pk;
    }

    public void setGsi3Pk(String gsi3Pk) {
        this.gsi3Pk = gsi3Pk;
    }

    @DynamoDbSecondarySortKey(indexNames = {"gsi3"})
    public String getGsi3Sk() {
        return gsi3Sk;
    }

    public void setGsi3Sk(String gsi3Sk) {
        this.gsi3Sk = gsi3Sk;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getMarkdownBody() {
        return markdownBody;
    }

    public void setMarkdownBody(String markdownBody) {
        this.markdownBody = markdownBody;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
