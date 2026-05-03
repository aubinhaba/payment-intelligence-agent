package com.aubin.pia.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aubin.pia.api.dto.MetricsSummaryResponse;
import com.aubin.pia.application.usecase.MetricsSummaryUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Metrics", description = "Business KPI summary for the dashboard")
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsSummaryUseCase metricsSummaryUseCase;

    public MetricsController(MetricsSummaryUseCase metricsSummaryUseCase) {
        this.metricsSummaryUseCase = metricsSummaryUseCase;
    }

    @Operation(
            summary = "Get metrics summary",
            description =
                    "Returns business KPIs: transaction count, anomaly count, report count and"
                            + " anomaly distribution by severity.")
    @GetMapping("/summary")
    public ResponseEntity<MetricsSummaryResponse> summary() {
        return ResponseEntity.ok(MetricsSummaryResponse.from(metricsSummaryUseCase.compute()));
    }
}
