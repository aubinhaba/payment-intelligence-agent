package com.aubin.pia.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aubin.pia.application.port.in.AnalyzeWithAgentCommand;
import com.aubin.pia.application.port.in.GenerateReportCommand;
import com.aubin.pia.domain.report.Report;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.transaction.TransactionId;

/**
 * Orchestrates the agent analysis and report generation pipeline.
 *
 * <p>Invoked by the asynchronous anomaly-analysis consumer: a Claude-powered analysis produces a
 * {@link ReportContent} which is then persisted as a {@link Report} via the report storage and
 * repository ports.
 */
public class AnalyzeAndReportUseCase {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeAndReportUseCase.class);

    private final AnalyzeWithAgentUseCase analyzeWithAgentUseCase;
    private final GenerateReportUseCase generateReportUseCase;

    public AnalyzeAndReportUseCase(
            AnalyzeWithAgentUseCase analyzeWithAgentUseCase,
            GenerateReportUseCase generateReportUseCase) {
        this.analyzeWithAgentUseCase = analyzeWithAgentUseCase;
        this.generateReportUseCase = generateReportUseCase;
    }

    public Report run(TransactionId transactionId) {
        log.info("analyze-and-report.start txId={}", transactionId.value());
        ReportContent content =
                analyzeWithAgentUseCase.analyze(new AnalyzeWithAgentCommand(transactionId));
        Report report =
                generateReportUseCase.generate(new GenerateReportCommand(transactionId, content));
        log.info(
                "analyze-and-report.done txId={} reportId={}",
                transactionId.value(),
                report.id().value());
        return report;
    }
}
