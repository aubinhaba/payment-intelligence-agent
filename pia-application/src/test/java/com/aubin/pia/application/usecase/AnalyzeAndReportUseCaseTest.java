package com.aubin.pia.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aubin.pia.application.port.in.AnalyzeWithAgentCommand;
import com.aubin.pia.application.port.in.GenerateReportCommand;
import com.aubin.pia.domain.report.Report;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.report.ReportId;
import com.aubin.pia.domain.transaction.TransactionId;

@ExtendWith(MockitoExtension.class)
class AnalyzeAndReportUseCaseTest {

    private static final TransactionId TX_ID = new TransactionId("tx_001");

    @Mock AnalyzeWithAgentUseCase analyzeWithAgentUseCase;
    @Mock GenerateReportUseCase generateReportUseCase;

    @InjectMocks AnalyzeAndReportUseCase useCase;

    @Test
    void should_chain_analyze_then_generate_and_return_report() {
        ReportContent content = new ReportContent("High risk", "## Details");
        Report report =
                Report.reconstitute(
                        new ReportId("rpt_001"),
                        TX_ID,
                        content,
                        Instant.parse("2026-04-18T10:00:00Z"));
        when(analyzeWithAgentUseCase.analyze(any(AnalyzeWithAgentCommand.class)))
                .thenReturn(content);
        when(generateReportUseCase.generate(any(GenerateReportCommand.class))).thenReturn(report);

        Report result = useCase.run(TX_ID);

        assertThat(result.id().value()).isEqualTo("rpt_001");
        verify(analyzeWithAgentUseCase).analyze(new AnalyzeWithAgentCommand(TX_ID));
        verify(generateReportUseCase).generate(new GenerateReportCommand(TX_ID, content));
    }

    @Test
    void should_propagate_agent_failure_without_generating_report() {
        when(analyzeWithAgentUseCase.analyze(any()))
                .thenThrow(new RuntimeException("claude API down"));

        assertThatThrownBy(() -> useCase.run(TX_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("claude API down");

        verify(generateReportUseCase, never()).generate(any());
    }
}
