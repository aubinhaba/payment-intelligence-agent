package com.aubin.pia.infrastructure.messaging.sqs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.usecase.AnalyzeAndReportUseCase;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.messaging.sqs.dto.AnomalyAnalysisRequestDto;

@ExtendWith(MockitoExtension.class)
class SqsAnomalyAnalysisListenerTest {

    @Mock private AnalyzeAndReportUseCase analyzeAndReportUseCase;
    @Mock private MetricsPublisher metricsPublisher;

    private SqsAnomalyAnalysisListener listener;

    @BeforeEach
    void setUp() {
        listener = new SqsAnomalyAnalysisListener(analyzeAndReportUseCase, metricsPublisher);
    }

    @Test
    void handle_valid_request_invokes_pipeline_and_records_consumption() {
        listener.handle(
                new AnomalyAnalysisRequestDto(
                        "tx_001", "corr-001", Instant.parse("2026-04-18T10:00:00Z")));

        verify(metricsPublisher).incrementSqsMessagesConsumed();
        verify(analyzeAndReportUseCase).run(new TransactionId("tx_001"));
        verify(metricsPublisher, never()).incrementAgentFailures();
    }

    @Test
    void handle_pipeline_failure_records_metric_and_swallows() {
        when(analyzeAndReportUseCase.run(any())).thenThrow(new RuntimeException("claude API down"));

        listener.handle(
                new AnomalyAnalysisRequestDto(
                        "tx_002", "corr-002", Instant.parse("2026-04-18T10:00:00Z")));

        verify(metricsPublisher).incrementAgentFailures();
    }
}
