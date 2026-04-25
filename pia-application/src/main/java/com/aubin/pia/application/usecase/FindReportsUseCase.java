package com.aubin.pia.application.usecase;

import java.util.List;
import java.util.Optional;

import com.aubin.pia.application.port.out.ReportRepository;
import com.aubin.pia.domain.report.Report;
import com.aubin.pia.domain.report.ReportId;

public class FindReportsUseCase {

    private final ReportRepository reportRepository;

    public FindReportsUseCase(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Report> findRecent(int limit) {
        return reportRepository.findRecent(limit);
    }

    public Optional<Report> findById(String reportId) {
        return reportRepository.findById(new ReportId(reportId));
    }
}
