package com.aubin.pia.application.port.out;

import java.util.List;
import java.util.Optional;

import com.aubin.pia.domain.report.Report;
import com.aubin.pia.domain.report.ReportId;

public interface ReportRepository {
    void save(Report report);

    Optional<Report> findById(ReportId id);

    List<Report> findRecent(int limit);
}
