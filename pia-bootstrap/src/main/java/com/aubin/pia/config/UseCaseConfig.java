package com.aubin.pia.config;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aubin.pia.application.policy.AmountDetectionPolicy;
import com.aubin.pia.application.policy.CardTestingDetectionPolicy;
import com.aubin.pia.application.policy.GeoDetectionPolicy;
import com.aubin.pia.application.policy.VelocityDetectionPolicy;
import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.application.port.out.EventPublisher;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.ReportRepository;
import com.aubin.pia.application.port.out.ReportStorage;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.application.usecase.DetectAnomaliesUseCase;
import com.aubin.pia.application.usecase.GenerateReportUseCase;
import com.aubin.pia.application.usecase.IngestTransactionUseCase;

/**
 * Wires application use cases and detection policies with their driven port implementations.
 *
 * <p>Use cases and policies are plain Java objects (no Spring annotations in pia-application);
 * Spring dependency injection happens here in the bootstrap module.
 *
 * <p>Phase 4 will wire {@code AnalyzeWithAgentUseCase} once {@code AgentPort} is implemented.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public IngestTransactionUseCase ingestTransactionUseCase(
            TransactionRepository transactionRepository,
            EventPublisher eventPublisher,
            MetricsPublisher metricsPublisher) {
        return new IngestTransactionUseCase(
                transactionRepository, eventPublisher, metricsPublisher);
    }

    @Bean
    public AmountDetectionPolicy amountDetectionPolicy(
            @Value("${pia.detection.amount.high-threshold}") BigDecimal highThreshold,
            @Value("${pia.detection.amount.critical-threshold}") BigDecimal criticalThreshold) {
        return new AmountDetectionPolicy(highThreshold, criticalThreshold);
    }

    @Bean
    public GeoDetectionPolicy geoDetectionPolicy(
            @Value("${pia.detection.geo.high-risk-countries}") String countriesCsv) {
        Set<String> countries =
                Arrays.stream(countriesCsv.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());
        return new GeoDetectionPolicy(countries);
    }

    @Bean
    public VelocityDetectionPolicy velocityDetectionPolicy(
            TransactionRepository transactionRepository,
            @Value("${pia.detection.velocity.window-hours}") int windowHours,
            @Value("${pia.detection.velocity.high-count}") int highCount,
            @Value("${pia.detection.velocity.critical-count}") int criticalCount) {
        return new VelocityDetectionPolicy(
                transactionRepository, windowHours, highCount, criticalCount);
    }

    @Bean
    public CardTestingDetectionPolicy cardTestingDetectionPolicy(
            TransactionRepository transactionRepository,
            @Value("${pia.detection.card-testing.window-hours}") int windowHours,
            @Value("${pia.detection.card-testing.min-count}") int minCount,
            @Value("${pia.detection.card-testing.high-count}") int highCount,
            @Value("${pia.detection.card-testing.max-micro-amount}") BigDecimal maxMicroAmount) {
        return new CardTestingDetectionPolicy(
                transactionRepository, windowHours, minCount, highCount, maxMicroAmount);
    }

    @Bean
    public DetectAnomaliesUseCase detectAnomaliesUseCase(
            TransactionRepository transactionRepository,
            AnomalyRepository anomalyRepository,
            EventPublisher eventPublisher,
            MetricsPublisher metricsPublisher,
            AmountDetectionPolicy amountDetectionPolicy,
            GeoDetectionPolicy geoDetectionPolicy,
            VelocityDetectionPolicy velocityDetectionPolicy,
            CardTestingDetectionPolicy cardTestingDetectionPolicy) {
        return new DetectAnomaliesUseCase(
                transactionRepository,
                anomalyRepository,
                eventPublisher,
                metricsPublisher,
                List.of(
                        amountDetectionPolicy,
                        geoDetectionPolicy,
                        velocityDetectionPolicy,
                        cardTestingDetectionPolicy));
    }

    @Bean
    public GenerateReportUseCase generateReportUseCase(
            ReportRepository reportRepository,
            ReportStorage reportStorage,
            MetricsPublisher metricsPublisher) {
        return new GenerateReportUseCase(reportRepository, reportStorage, metricsPublisher);
    }
}
