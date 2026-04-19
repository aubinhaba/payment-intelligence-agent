package com.aubin.pia.application.port.in;

import java.util.Objects;

import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.transaction.TransactionId;

public record GenerateReportCommand(TransactionId transactionId, ReportContent content) {
    public GenerateReportCommand {
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(content, "content must not be null");
    }
}
