package com.aubin.pia.application.port.out;

import com.aubin.pia.domain.report.Report;

public interface ReportStorage {
    /** Stores the report and returns the storage URI (e.g. S3 key). */
    String store(Report report);
}
