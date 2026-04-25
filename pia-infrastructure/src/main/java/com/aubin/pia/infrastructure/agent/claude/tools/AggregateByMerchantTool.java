package com.aubin.pia.infrastructure.agent.claude.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.transaction.Transaction;

public class AggregateByMerchantTool implements AgentTool {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper mapper;

    public AggregateByMerchantTool(
            TransactionRepository transactionRepository, ObjectMapper mapper) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    @Override
    public String name() {
        return "aggregate_by_merchant";
    }

    @Override
    public String description() {
        return "Aggregates transaction statistics (count, total, average amount) for a given"
                + " merchant within a time window.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties",
                        Map.of(
                                "merchant_id",
                                Map.of("type", "string", "description", "Merchant identifier"),
                                "window_hours",
                                Map.of(
                                        "type",
                                        "integer",
                                        "description",
                                        "Look-back window in hours")),
                "required", List.of("merchant_id", "window_hours"));
    }

    @Override
    public String execute(JsonNode input) {
        String merchantId = input.required("merchant_id").asText();
        int windowHours = input.required("window_hours").asInt();
        List<Transaction> transactions =
                transactionRepository.findByMerchantId(merchantId, windowHours);

        int count = transactions.size();
        BigDecimal total =
                transactions.stream()
                        .map(tx -> tx.amount().value())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average =
                count == 0
                        ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                        : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        ObjectNode result = mapper.createObjectNode();
        result.put("merchant_id", merchantId);
        result.put("transaction_count", count);
        result.put("total_amount", total.setScale(2, RoundingMode.HALF_UP).toPlainString());
        result.put("average_amount", average.toPlainString());

        try {
            return mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise merchant aggregation", e);
        }
    }
}
