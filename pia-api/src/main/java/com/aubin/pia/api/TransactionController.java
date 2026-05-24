package com.aubin.pia.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aubin.pia.api.dto.PagedResponse;
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
            summary = "List transactions (paginated)",
            description =
                    "Returns a page of transactions ordered by scan order. page is 0-based, size"
                            + " capped at 100.")
    @GetMapping
    public ResponseEntity<PagedResponse<TransactionResponse>> findPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        List<TransactionResponse> content =
                findTransactionsUseCase.findPaged(safePage, safeSize).stream()
                        .map(TransactionResponse::from)
                        .toList();
        return ResponseEntity.ok(PagedResponse.of(content, safePage, safeSize));
    }
}
