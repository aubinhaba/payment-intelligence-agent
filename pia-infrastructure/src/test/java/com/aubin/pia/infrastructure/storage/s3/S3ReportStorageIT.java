package com.aubin.pia.infrastructure.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

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

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Testcontainers
class S3ReportStorageIT {

    private static final String BUCKET = "pia-test-reports";

    @Container
    static final LocalStackContainer LOCAL_STACK = LocalStackTestSupport.newContainer(Service.S3);

    private S3ReportStorage storage;
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        s3Client =
                S3Client.builder()
                        .endpointOverride(
                                URI.create(LOCAL_STACK.getEndpointOverride(Service.S3).toString()))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create("test", "test")))
                        .region(Region.EU_WEST_1)
                        .forcePathStyle(true)
                        .build();

        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
        storage = new S3ReportStorage(s3Client, BUCKET);
    }

    @Test
    void store_uploads_markdown_and_returns_key() {
        Report report =
                Report.generate(
                        new ReportId("rep-s3-001"),
                        new TransactionId("tx-001"),
                        new ReportContent(
                                "Risk detected", "## Analysis\n\nHigh-risk transaction."));

        String key = storage.store(report);

        assertThat(key).startsWith("reports/").endsWith(".md");

        String body =
                s3Client.getObjectAsBytes(
                                GetObjectRequest.builder().bucket(BUCKET).key(key).build())
                        .asUtf8String();
        assertThat(body).contains("High-risk transaction");
    }
}
