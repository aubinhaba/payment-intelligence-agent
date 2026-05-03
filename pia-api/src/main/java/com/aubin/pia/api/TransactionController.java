package com.aubin.pia.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aubin.pia.api.dto.TransactionResponse;
import com.aubin.pia.application.usecase.FindTransactionsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Transactions", description = "Query ingested payment transactions")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final FindTransactionsUseCase findTransactionsUseCase;

    public TransactionController(FindTransactionsUseCase findTransactionsUseCase) {
        this.findTransactionsUseCase = findTransactionsUseCase;
    }

    @Operation(
            summary = "List recent transactions",
            description = "Returns up to `limit` most recently ingested transactions.")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findRecent(
            @RequestParam(defaultValue = "20") int limit) {
        List<TransactionResponse> body =
                findTransactionsUseCase.findRecent(limit).stream()
                        .map(TransactionResponse::from)
                        .toList();
        return ResponseEntity.ok(body);
    }
}
