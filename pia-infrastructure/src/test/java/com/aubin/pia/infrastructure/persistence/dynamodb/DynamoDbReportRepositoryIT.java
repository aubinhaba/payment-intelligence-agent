package com.aubin.pia.infrastructure.persistence.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.aubin.pia.domain.report.Report;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.report.ReportId;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.LocalStackTestSupport;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Testcontainers
class DynamoDbReportRepositoryIT {

    @Container
    static final LocalStackContainer LOCAL_STACK =
            LocalStackTestSupport.newContainer(Service.DYNAMODB);

    private DynamoDbReportRepository repository;

    @BeforeEach
    void setUp() {
        DynamoDbClient client = LocalStackTestSupport.dynamoDbClient(LOCAL_STACK);
        LocalStackTestSupport.createPiaTable(client);
        DynamoDbEnhancedClient enhancedClient = LocalStackTestSupport.enhancedClient(LOCAL_STACK);
        repository = new DynamoDbReportRepository(enhancedClient, LocalStackTestSupport.TABLE_NAME);
    }

    @Test
    void save_then_findById_roundTrip() {
        Report report =
                Report.generate(
                        new ReportId("rep-001"),
                        new TransactionId("tx-001"),
                        new ReportContent(
                                "High-risk transaction detected",
                                "## Analysis\n\nTransaction shows anomalous pattern."));

        repository.save(report);

        Optional<Report> found = repository.findById(new ReportId("rep-001"));
        assertThat(found).isPresent();
        assertThat(found.get().id().value()).isEqualTo("rep-001");
        assertThat(found.get().transactionId().value()).isEqualTo("tx-001");
        assertThat(found.get().content().summary()).isEqualTo("High-risk transaction detected");
        assertThat(found.get().content().markdownBody()).contains("anomalous pattern");
    }

    @Test
    void findById_missing_returns_empty() {
        Optional<Report> found = repository.findById(new ReportId("rep-unknown"));
        assertThat(found).isEmpty();
    }
}
