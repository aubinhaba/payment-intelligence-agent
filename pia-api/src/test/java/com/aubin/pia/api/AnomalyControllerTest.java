package com.aubin.pia.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aubin.pia.application.usecase.FindAnomaliesUseCase;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.TransactionId;

@WebMvcTest(AnomalyController.class)
class AnomalyControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean FindAnomaliesUseCase findAnomaliesUseCase;

    @Test
    void get_anomalies_returns_200_with_paged_content() throws Exception {
        when(findAnomaliesUseCase.findPaged(0, 20)).thenReturn(List.of(sampleAnomaly()));

        mockMvc.perform(get("/api/anomalies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("an_001"))
                .andExpect(jsonPath("$.content[0].transactionId").value("tx_001"))
                .andExpect(jsonPath("$.content[0].type").value("VELOCITY"))
                .andExpect(jsonPath("$.content[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void get_anomalies_returns_empty_content_when_none() throws Exception {
        when(findAnomaliesUseCase.findPaged(0, 20)).thenReturn(List.of());

        mockMvc.perform(get("/api/anomalies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void get_anomalies_respects_page_and_size_params() throws Exception {
        when(findAnomaliesUseCase.findPaged(2, 5)).thenReturn(List.of());

        mockMvc.perform(get("/api/anomalies").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5));
    }

    private Anomaly sampleAnomaly() {
        return Anomaly.reconstitute(
                new AnomalyId("an_001"),
                new TransactionId("tx_001"),
                AnomalyType.VELOCITY,
                Severity.HIGH,
                "Too many transactions in 1 hour",
                Instant.parse("2026-04-25T10:01:00Z"));
    }
}
