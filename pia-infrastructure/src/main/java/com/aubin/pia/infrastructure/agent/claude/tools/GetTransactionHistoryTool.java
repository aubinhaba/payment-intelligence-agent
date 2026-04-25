package com.aubin.pia.infrastructure.agent.claude.tools;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.transaction.Transaction;

public class GetTransactionHistoryTool implements AgentTool {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper mapper;

    public GetTransactionHistoryTool(
            TransactionRepository transactionRepository, ObjectMapper mapper) {
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    @Override
    public String name() {
        return "get_transaction_history";
    }

    @Override
    public String description() {
        return "Returns the recent transaction history for a given card reference hash within a"
                + " time window.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties",
                        Map.of(
                                "card_reference_hash",
                                Map.of("type", "string", "description", "Tokenised card hash"),
                                "window_hours",
                                Map.of(
                                        "type",
                                        "integer",
                                        "description",
                                        "Look-back window in hours")),
                "required", List.of("card_reference_hash", "window_hours"));
    }

    @Override
    public String execute(JsonNode input) {
        String cardHash = input.required("card_reference_hash").asText();
        int windowHours = input.required("window_hours").asInt();
        List<Transaction> transactions =
                transactionRepository.findByCardReference(cardHash, windowHours);

        ArrayNode result = mapper.createArrayNode();
        for (Transaction tx : transactions) {
            result.addObject()
                    .put("id", tx.id().value())
                    .put("amount", tx.amount().value().toPlainString())
                    .put("currency", tx.amount().currency().getCurrencyCode())
                    .put("merchant_id", tx.merchant().id())
                    .put("merchant_country", tx.merchant().country())
                    .put("occurred_at", tx.occurredAt().toString())
                    .put("status", tx.status().name());
        }
        try {
            return mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise transaction history", e);
        }
    }
}
