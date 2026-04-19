package com.aubin.pia.application.usecase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.application.port.out.EventPublisher;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.DetectionPolicy;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;

public class DetectAnomaliesUseCase {
    private final TransactionRepository transactionRepository;
    private final AnomalyRepository anomalyRepository;
    private final EventPublisher eventPublisher;
    private final MetricsPublisher metricsPublisher;
    private final List<DetectionPolicy> policies;

    public DetectAnomaliesUseCase(
            TransactionRepository transactionRepository,
            AnomalyRepository anomalyRepository,
            EventPublisher eventPublisher,
            MetricsPublisher metricsPublisher,
            List<DetectionPolicy> policies) {
        this.transactionRepository = transactionRepository;
        this.anomalyRepository = anomalyRepository;
        this.eventPublisher = eventPublisher;
        this.metricsPublisher = metricsPublisher;
        this.policies = Collections.unmodifiableList(new ArrayList<>(policies));
    }

    public List<Anomaly> detect(TransactionId transactionId) {
        Transaction transaction =
                transactionRepository
                        .findById(transactionId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Transaction not found: " + transactionId.value()));

        List<Anomaly> anomalies =
                policies.stream()
                        .map(policy -> policy.detect(transaction))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();

        anomalies.forEach(
                anomaly -> {
                    anomalyRepository.save(anomaly);
                    eventPublisher.publishAll(anomaly.pullDomainEvents());
                    metricsPublisher.incrementAnomaliesDetected(anomaly.type(), anomaly.severity());
                });

        return anomalies;
    }
}
