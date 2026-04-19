package com.aubin.pia.application.usecase;

import java.util.UUID;

import com.aubin.pia.application.port.in.GenerateReportCommand;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.ReportRepository;
import com.aubin.pia.application.port.out.ReportStorage;
import com.aubin.pia.domain.report.Report;
import com.aubin.pia.domain.report.ReportId;

public class GenerateReportUseCase {
    private final ReportRepository reportRepository;
    private final ReportStorage reportStorage;
    private final MetricsPublisher metricsPublisher;

    public GenerateReportUseCase(
            ReportRepository reportRepository,
            ReportStorage reportStorage,
            MetricsPublisher metricsPublisher) {
        this.reportRepository = reportRepository;
        this.reportStorage = reportStorage;
        this.metricsPublisher = metricsPublisher;
    }

    public Report generate(GenerateReportCommand command) {
        Report report =
                Report.generate(
                        new ReportId(UUID.randomUUID().toString()),
                        command.transactionId(),
                        command.content());
        reportStorage.store(report);
        reportRepository.save(report);
        metricsPublisher.incrementReportsGenerated();
        return report;
    }
}
