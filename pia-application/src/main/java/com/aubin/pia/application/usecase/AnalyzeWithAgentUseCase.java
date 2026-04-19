package com.aubin.pia.application.usecase;

import java.util.List;

import com.aubin.pia.application.port.in.AnalyzeWithAgentCommand;
import com.aubin.pia.application.port.out.AgentPort;
import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.transaction.Transaction;

public class AnalyzeWithAgentUseCase {
    private final TransactionRepository transactionRepository;
    private final AnomalyRepository anomalyRepository;
    private final AgentPort agentPort;
    private final MetricsPublisher metricsPublisher;

    public AnalyzeWithAgentUseCase(
            TransactionRepository transactionRepository,
            AnomalyRepository anomalyRepository,
            AgentPort agentPort,
            MetricsPublisher metricsPublisher) {
        this.transactionRepository = transactionRepository;
        this.anomalyRepository = anomalyRepository;
        this.agentPort = agentPort;
        this.metricsPublisher = metricsPublisher;
    }

    public ReportContent analyze(AnalyzeWithAgentCommand command) {
        Transaction transaction =
                transactionRepository
                        .findById(command.transactionId())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Transaction not found: "
                                                        + command.transactionId().value()));

        List<Anomaly> anomalies = anomalyRepository.findByTransactionId(command.transactionId());

        long start = System.currentTimeMillis();
        ReportContent content = agentPort.analyze(transaction, anomalies);
        metricsPublisher.recordAgentLatencyMillis(System.currentTimeMillis() - start);
        return content;
    }
}
