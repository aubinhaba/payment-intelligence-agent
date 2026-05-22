package com.aubin.pia.infrastructure.messaging.sqs;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aubin.pia.application.port.out.AnalysisRequestPublisher;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.messaging.sqs.dto.AnomalyAnalysisRequestDto;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@Component
public class SqsAnalysisRequestPublisher implements AnalysisRequestPublisher {

    private static final Logger log = LoggerFactory.getLogger(SqsAnalysisRequestPublisher.class);

    private final SqsTemplate sqsTemplate;
    private final String queueName;

    @SuppressWarnings({"EI_EXPOSE_REP2"}) // SqsTemplate is a managed Spring bean
    public SqsAnalysisRequestPublisher(
            SqsTemplate sqsTemplate, @Value("${pia.sqs.anomaly-analysis-queue}") String queueName) {
        this.sqsTemplate = sqsTemplate;
        this.queueName = queueName;
    }

    @Override
    public void publish(TransactionId transactionId) {
        String correlationId = currentCorrelationId();
        AnomalyAnalysisRequestDto dto =
                new AnomalyAnalysisRequestDto(transactionId.value(), correlationId, Instant.now());
        sqsTemplate.send(queueName, dto);
        log.info(
                "analysis.requested txId={} correlationId={} queue={}",
                transactionId.value(),
                correlationId,
                queueName);
    }

    private static String currentCorrelationId() {
        String mdcValue = MDC.get("correlationId");
        return mdcValue != null ? mdcValue : UUID.randomUUID().toString();
    }
}
