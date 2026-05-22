package com.aubin.pia.infrastructure.messaging.sqs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.messaging.sqs.dto.AnomalyAnalysisRequestDto;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@ExtendWith(MockitoExtension.class)
class SqsAnalysisRequestPublisherTest {

    private static final String QUEUE = "anomaly-analysis-queue";

    @Mock private SqsTemplate sqsTemplate;

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void publish_sends_dto_with_correlation_id_from_mdc() {
        MDC.put("correlationId", "corr-abc");
        SqsAnalysisRequestPublisher publisher = new SqsAnalysisRequestPublisher(sqsTemplate, QUEUE);

        publisher.publish(new TransactionId("tx_001"));

        ArgumentCaptor<AnomalyAnalysisRequestDto> captor =
                ArgumentCaptor.forClass(AnomalyAnalysisRequestDto.class);
        verify(sqsTemplate).send(eq(QUEUE), captor.capture());
        AnomalyAnalysisRequestDto sent = captor.getValue();
        assertThat(sent.transactionId()).isEqualTo("tx_001");
        assertThat(sent.correlationId()).isEqualTo("corr-abc");
        assertThat(sent.requestedAt()).isNotNull();
    }

    @Test
    void publish_falls_back_to_random_correlation_id_when_mdc_empty() {
        SqsAnalysisRequestPublisher publisher = new SqsAnalysisRequestPublisher(sqsTemplate, QUEUE);

        publisher.publish(new TransactionId("tx_002"));

        ArgumentCaptor<AnomalyAnalysisRequestDto> captor =
                ArgumentCaptor.forClass(AnomalyAnalysisRequestDto.class);
        verify(sqsTemplate).send(eq(QUEUE), captor.capture());
        assertThat(captor.getValue().correlationId()).isNotBlank();
    }
}
