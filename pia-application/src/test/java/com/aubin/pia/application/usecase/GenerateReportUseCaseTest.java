package com.aubin.pia.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aubin.pia.application.port.in.GenerateReportCommand;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.ReportRepository;
import com.aubin.pia.application.port.out.ReportStorage;
import com.aubin.pia.domain.report.Report;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.transaction.TransactionId;

@ExtendWith(MockitoExtension.class)
class GenerateReportUseCaseTest {

    @Mock ReportRepository reportRepository;
    @Mock ReportStorage reportStorage;
    @Mock MetricsPublisher metricsPublisher;

    @InjectMocks GenerateReportUseCase useCase;

    @Test
    void should_store_and_persist_report() {
        when(reportStorage.store(any(Report.class))).thenReturn("s3://bucket/report.json");
        GenerateReportCommand command =
                new GenerateReportCommand(
                        new TransactionId("tx_001"),
                        new ReportContent("Risk summary", "## Details"));

        Report report = useCase.generate(command);

        assertThat(report.id().value()).isNotBlank();
        assertThat(report.transactionId().value()).isEqualTo("tx_001");
        verify(reportStorage).store(any(Report.class));
        verify(reportRepository).save(any(Report.class));
        verify(metricsPublisher).incrementReportsGenerated();
    }

    @Test
    void should_generate_unique_report_ids_for_same_transaction() {
        when(reportStorage.store(any())).thenReturn("s3://key");
        GenerateReportCommand command =
                new GenerateReportCommand(
                        new TransactionId("tx_001"), new ReportContent("Summary", "body"));

        Report r1 = useCase.generate(command);
        Report r2 = useCase.generate(command);

        assertThat(r1.id().value()).isNotEqualTo(r2.id().value());
    }
}
