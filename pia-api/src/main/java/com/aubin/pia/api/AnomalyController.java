package com.aubin.pia.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aubin.pia.api.dto.AnomalyResponse;
import com.aubin.pia.application.usecase.FindAnomaliesUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Anomalies", description = "Query detected payment anomalies")
@RestController
@RequestMapping("/api/anomalies")
public class AnomalyController {

    private final FindAnomaliesUseCase findAnomaliesUseCase;

    public AnomalyController(FindAnomaliesUseCase findAnomaliesUseCase) {
        this.findAnomaliesUseCase = findAnomaliesUseCase;
    }

    @Operation(
            summary = "List recent anomalies",
            description = "Returns up to `limit` most recently detected anomalies.")
    @GetMapping
    public ResponseEntity<List<AnomalyResponse>> findRecent(
            @RequestParam(defaultValue = "20") int limit) {
        List<AnomalyResponse> body =
                findAnomaliesUseCase.findRecent(limit).stream().map(AnomalyResponse::from).toList();
        return ResponseEntity.ok(body);
    }
}
