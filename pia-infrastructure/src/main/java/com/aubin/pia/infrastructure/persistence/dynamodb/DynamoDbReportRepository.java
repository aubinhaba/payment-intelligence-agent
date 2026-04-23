package com.aubin.pia.infrastructure.persistence.dynamodb;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.aubin.pia.application.port.out.ReportRepository;
import com.aubin.pia.domain.report.Report;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.report.ReportId;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.persistence.dynamodb.entity.ReportEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class DynamoDbReportRepository implements ReportRepository {

    static final String PK_PREFIX = "REPORT#";
    static final String SK_METADATA = "METADATA";
    static final String GSI3_PK_DAILY = "REPORT#daily";

    private final DynamoDbTable<ReportEntity> table;

    public DynamoDbReportRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${pia.dynamodb.table-name}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(ReportEntity.class));
    }

    @Override
    public void save(Report report) {
        table.putItem(toEntity(report));
    }

    @Override
    public Optional<Report> findById(ReportId id) {
        Key key =
                Key.builder().partitionValue(PK_PREFIX + id.value()).sortValue(SK_METADATA).build();
        return Optional.ofNullable(table.getItem(key)).map(this::toDomain);
    }

    private ReportEntity toEntity(Report report) {
        ReportEntity e = new ReportEntity();
        e.setPk(PK_PREFIX + report.id().value());
        e.setSk(SK_METADATA);
        e.setGsi3Pk(GSI3_PK_DAILY);
        e.setGsi3Sk(report.generatedAt().toString().substring(0, 10));
        e.setReportId(report.id().value());
        e.setTxId(report.transactionId().value());
        e.setSummary(report.content().summary());
        e.setMarkdownBody(report.content().markdownBody());
        e.setGeneratedAt(report.generatedAt().toString());
        return e;
    }

    private Report toDomain(ReportEntity e) {
        return Report.reconstitute(
                new ReportId(e.getReportId()),
                new TransactionId(e.getTxId()),
                new ReportContent(e.getSummary(), e.getMarkdownBody()),
                Instant.parse(e.getGeneratedAt()));
    }
}
