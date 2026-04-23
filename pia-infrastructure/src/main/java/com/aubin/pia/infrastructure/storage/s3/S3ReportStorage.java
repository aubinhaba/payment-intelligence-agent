package com.aubin.pia.infrastructure.storage.s3;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aubin.pia.application.port.out.ReportStorage;
import com.aubin.pia.domain.report.Report;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3ReportStorage implements ReportStorage {

    private static final Logger log = LoggerFactory.getLogger(S3ReportStorage.class);

    private final S3Client s3Client;
    private final String bucket;

    public S3ReportStorage(S3Client s3Client, @Value("${pia.s3.reports-bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String store(Report report) {
        String date = LocalDate.ofInstant(report.generatedAt(), ZoneOffset.UTC).toString();
        String key = "reports/" + date + "/" + report.id().value() + ".md";

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("text/markdown; charset=UTF-8")
                        .build(),
                RequestBody.fromString(report.content().markdownBody()));

        log.info("report.stored reportId={} s3Key={}", report.id().value(), key);
        return key;
    }
}
