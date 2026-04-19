package com.aubin.pia.application.port.out;

import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;

public interface MetricsPublisher {
    void incrementTransactionsIngested();

    void incrementAnomaliesDetected(AnomalyType type, Severity severity);

    void incrementReportsGenerated();

    void recordAgentLatencyMillis(long millis);
}
