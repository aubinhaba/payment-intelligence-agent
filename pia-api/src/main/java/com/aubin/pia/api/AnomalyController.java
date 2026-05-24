package com.aubin.pia.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aubin.pia.api.dto.AnomalyResponse;
import com.aubin.pia.api.dto.PagedResponse;
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
            summary = "List anomalies (paginated)",
            description = "Returns a page of anomalies. page is 0-based, size capped at 100.")
    @GetMapping
    public ResponseEntity<PagedResponse<AnomalyResponse>> findPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        List<AnomalyResponse> content =
                findAnomaliesUseCase.findPaged(safePage, safeSize).stream()
                        .map(AnomalyResponse::from)
                        .toList();
        return ResponseEntity.ok(PagedResponse.of(content, safePage, safeSize));
    }
}
