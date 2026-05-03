package com.aubin.pia.application.usecase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.application.port.out.ReportRepository;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.anomaly.Anomaly;

public class MetricsSummaryUseCase {

    private static final int SAMPLE_LIMIT = 1_000;

    private final TransactionRepository transactionRepository;
    private final AnomalyRepository anomalyRepository;
    private final ReportRepository reportRepository;

    public MetricsSummaryUseCase(
            TransactionRepository transactionRepository,
            AnomalyRepository anomalyRepository,
            ReportRepository reportRepository) {
        this.transactionRepository = transactionRepository;
        this.anomalyRepository = anomalyRepository;
        this.reportRepository = reportRepository;
    }

    public Summary compute() {
        int txCount = transactionRepository.findRecent(SAMPLE_LIMIT).size();
        List<Anomaly> anomalies = anomalyRepository.findRecent(SAMPLE_LIMIT);
        int reportCount = reportRepository.findRecent(SAMPLE_LIMIT).size();

        Map<String, Long> anomaliesBySeverity =
                anomalies.stream()
                        .collect(
                                Collectors.groupingBy(
                                        a -> a.severity().name(),
                                        LinkedHashMap::new,
                                        Collectors.counting()));

        return new Summary(txCount, anomalies.size(), reportCount, anomaliesBySeverity);
    }

    public record Summary(
            int transactionCount,
            int anomalyCount,
            int reportCount,
            Map<String, Long> anomaliesBySeverity) {
        public Summary {
            anomaliesBySeverity = Map.copyOf(anomaliesBySeverity);
        }
    }
}
