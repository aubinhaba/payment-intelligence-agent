package com.aubin.pia.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aubin.pia.api.dto.ReportResponse;
import com.aubin.pia.application.usecase.FindReportsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reports", description = "Query AI-generated analysis reports")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final FindReportsUseCase findReportsUseCase;

    public ReportController(FindReportsUseCase findReportsUseCase) {
        this.findReportsUseCase = findReportsUseCase;
    }

    @Operation(summary = "List recent reports")
    @GetMapping
    public ResponseEntity<List<ReportResponse>> findRecent(
            @RequestParam(defaultValue = "20") int limit) {
        List<ReportResponse> body =
                findReportsUseCase.findRecent(limit).stream().map(ReportResponse::from).toList();
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Get report by id")
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> findById(@PathVariable String id) {
        return findReportsUseCase
                .findById(id)
                .map(ReportResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
