package com.aubin.pia.infrastructure.messaging.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.usecase.AnalyzeAndReportUseCase;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.messaging.sqs.dto.AnomalyAnalysisRequestDto;

import io.awspring.cloud.sqs.annotation.SqsListener;

/**
 * Consumes anomaly-analysis requests and runs the Claude analysis + report generation pipeline.
 *
 * <p>Failures are logged + counted but the message is still ACKed (best-effort). The upstream
 * transaction and its anomalies are already persisted, so a missing report is acceptable — alerting
 * lives on {@code pia.agent.failures.total}.
 */
@Component
public class SqsAnomalyAnalysisListener {

    private static final Logger log = LoggerFactory.getLogger(SqsAnomalyAnalysisListener.class);

    private final AnalyzeAndReportUseCase analyzeAndReportUseCase;
    private final MetricsPublisher metricsPublisher;

    public SqsAnomalyAnalysisListener(
            AnalyzeAndReportUseCase analyzeAndReportUseCase, MetricsPublisher metricsPublisher) {
        this.analyzeAndReportUseCase = analyzeAndReportUseCase;
        this.metricsPublisher = metricsPublisher;
    }

    @SqsListener("${pia.sqs.anomaly-analysis-queue}")
    public void handle(AnomalyAnalysisRequestDto request) {
        MDC.put("correlationId", request.correlationId());
        MDC.put("transactionId", request.transactionId());
        try {
            metricsPublisher.incrementSqsMessagesConsumed();
            analyzeAndReportUseCase.run(new TransactionId(request.transactionId()));
        } catch (RuntimeException ex) {
            metricsPublisher.incrementAgentFailures();
            log.error(
                    "analysis.failed txId={} reason={}",
                    request.transactionId(),
                    ex.getMessage(),
                    ex);
        } finally {
            MDC.clear();
        }
    }
}
