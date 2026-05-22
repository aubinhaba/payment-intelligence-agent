package com.aubin.pia.infrastructure.messaging.sqs;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aubin.pia.application.port.in.IngestTransactionCommand;
import com.aubin.pia.application.port.out.AnalysisRequestPublisher;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.application.usecase.DetectAnomaliesUseCase;
import com.aubin.pia.application.usecase.IngestTransactionUseCase;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto;

import io.awspring.cloud.sqs.annotation.SqsListener;

@Component
public class SqsPaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(SqsPaymentEventListener.class);

    private final IngestTransactionUseCase ingestTransactionUseCase;
    private final DetectAnomaliesUseCase detectAnomaliesUseCase;
    private final AnalysisRequestPublisher analysisRequestPublisher;
    private final TransactionRepository transactionRepository;
    private final MetricsPublisher metricsPublisher;
    private final Severity triggerMinSeverity;

    public SqsPaymentEventListener(
            IngestTransactionUseCase ingestTransactionUseCase,
            DetectAnomaliesUseCase detectAnomaliesUseCase,
            AnalysisRequestPublisher analysisRequestPublisher,
            TransactionRepository transactionRepository,
            MetricsPublisher metricsPublisher,
            @Value("${pia.agent.trigger-min-severity:HIGH}") Severity triggerMinSeverity) {
        this.ingestTransactionUseCase = ingestTransactionUseCase;
        this.detectAnomaliesUseCase = detectAnomaliesUseCase;
        this.analysisRequestPublisher = analysisRequestPublisher;
        this.transactionRepository = transactionRepository;
        this.metricsPublisher = metricsPublisher;
        this.triggerMinSeverity = triggerMinSeverity;
    }

    @SqsListener("${pia.sqs.payment-events-queue}")
    public void handle(PaymentEventDto event) {
        MDC.put("correlationId", event.metadata().correlationId());
        MDC.put("transactionId", event.transaction().id());
        MDC.put("eventId", event.eventId());
        try {
            metricsPublisher.incrementSqsMessagesConsumed();
            processEvent(event);
        } finally {
            MDC.clear();
        }
    }

    private void processEvent(PaymentEventDto event) {
        TransactionId txId = new TransactionId(event.transaction().id());
        Optional<Transaction> existing = transactionRepository.findById(txId);
        if (existing.isPresent()) {
            log.debug("Duplicate event skipped transactionId={}", txId.value());
            return;
        }

        IngestTransactionCommand command =
                new IngestTransactionCommand(
                        event.transaction().id(),
                        event.transaction().amount().value(),
                        event.transaction().amount().currency(),
                        event.transaction().cardReference().hash(),
                        event.transaction().cardReference().last4(),
                        event.transaction().merchant().id(),
                        event.transaction().merchant().mcc(),
                        event.transaction().merchant().country(),
                        event.transaction().status(),
                        event.occurredAt());

        ingestTransactionUseCase.ingest(command);
        log.info("event.ingested transactionId={}", txId.value());

        List<Anomaly> anomalies = detectAnomaliesUseCase.detect(txId);
        if (anomalies.isEmpty()) {
            return;
        }
        log.info("event.anomalies transactionId={} count={}", txId.value(), anomalies.size());

        if (shouldTriggerAgent(anomalies)) {
            analysisRequestPublisher.publish(txId);
        }
    }

    private boolean shouldTriggerAgent(List<Anomaly> anomalies) {
        return anomalies.stream()
                .anyMatch(anomaly -> anomaly.severity().compareTo(triggerMinSeverity) >= 0);
    }
}
