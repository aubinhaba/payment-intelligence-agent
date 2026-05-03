package com.aubin.pia.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aubin.pia.application.usecase.MetricsSummaryUseCase;
import com.aubin.pia.application.usecase.MetricsSummaryUseCase.Summary;

@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean MetricsSummaryUseCase metricsSummaryUseCase;

    @Test
    void get_metrics_summary_returns_200_with_kpis() throws Exception {
        Summary summary = new Summary(42, 7, 3, Map.of("HIGH", 5L, "CRITICAL", 2L));
        when(metricsSummaryUseCase.compute()).thenReturn(summary);

        mockMvc.perform(get("/api/metrics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCount").value(42))
                .andExpect(jsonPath("$.anomalyCount").value(7))
                .andExpect(jsonPath("$.reportCount").value(3))
                .andExpect(jsonPath("$.anomaliesBySeverity.HIGH").value(5))
                .andExpect(jsonPath("$.anomaliesBySeverity.CRITICAL").value(2));
    }
}
