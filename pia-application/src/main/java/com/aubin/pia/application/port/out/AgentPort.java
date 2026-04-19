package com.aubin.pia.application.port.out;

import java.util.List;

import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.transaction.Transaction;

public interface AgentPort {
    ReportContent analyze(Transaction transaction, List<Anomaly> anomalies);
}
