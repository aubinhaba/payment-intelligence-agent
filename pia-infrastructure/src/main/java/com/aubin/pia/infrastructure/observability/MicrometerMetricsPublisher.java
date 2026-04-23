package com.aubin.pia.infrastructure.observability;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MicrometerMetricsPublisher implements MetricsPublisher {

    private final MeterRegistry registry;

    public MicrometerMetricsPublisher(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void incrementTransactionsIngested() {
        registry.counter("pia.transactions.ingested.total").increment();
    }

    @Override
    public void incrementAnomaliesDetected(AnomalyType type, Severity severity) {
        registry.counter(
                        "pia.anomalies.detected.total",
                        "type",
                        type.name(),
                        "severity",
                        severity.name())
                .increment();
    }

    @Override
    public void incrementReportsGenerated() {
        registry.counter("pia.reports.generated.total").increment();
    }

    @Override
    public void recordAgentLatencyMillis(long millis) {
        registry.timer("pia.agent.claude.latency").record(millis, TimeUnit.MILLISECONDS);
    }
}
