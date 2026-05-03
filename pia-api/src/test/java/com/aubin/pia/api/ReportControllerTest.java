package com.aubin.pia.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aubin.pia.application.usecase.FindReportsUseCase;
import com.aubin.pia.domain.report.Report;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.report.ReportId;
import com.aubin.pia.domain.transaction.TransactionId;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean FindReportsUseCase findReportsUseCase;

    @Test
    void get_reports_returns_200_with_list() throws Exception {
        when(findReportsUseCase.findRecent(20)).thenReturn(List.of(sampleReport()));

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("rpt_001"))
                .andExpect(jsonPath("$[0].transactionId").value("tx_001"))
                .andExpect(jsonPath("$[0].summary").value("High risk detected"));
    }

    @Test
    void get_report_by_id_returns_200() throws Exception {
        when(findReportsUseCase.findById("rpt_001")).thenReturn(Optional.of(sampleReport()));

        mockMvc.perform(get("/api/reports/rpt_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rpt_001"));
    }

    @Test
    void get_report_by_id_returns_404_when_not_found() throws Exception {
        when(findReportsUseCase.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reports/unknown")).andExpect(status().isNotFound());
    }

    private Report sampleReport() {
        return Report.reconstitute(
                new ReportId("rpt_001"),
                new TransactionId("tx_001"),
                new ReportContent("High risk detected", "## Analysis\nVelocity anomaly detected."),
                Instant.parse("2026-04-25T10:05:00Z"));
    }
}
