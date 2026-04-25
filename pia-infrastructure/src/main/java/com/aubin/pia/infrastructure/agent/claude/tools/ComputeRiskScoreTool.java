package com.aubin.pia.infrastructure.agent.claude.tools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;

public class ComputeRiskScoreTool implements AgentTool {

    private static final BigDecimal AMOUNT_MEDIUM = BigDecimal.valueOf(1_000);
    private static final BigDecimal AMOUNT_HIGH = BigDecimal.valueOf(5_000);
    private static final BigDecimal AMOUNT_CRITICAL = BigDecimal.valueOf(10_000);

    private final TransactionRepository transactionRepository;
    private final AnomalyRepository anomalyRepository;
    private final Set<String> highRiskCountries;
    private final ObjectMapper mapper;

    public ComputeRiskScoreTool(
            TransactionRepository transactionRepository,
            AnomalyRepository anomalyRepository,
            Set<String> highRiskCountries,
            ObjectMapper mapper) {
        this.transactionRepository = transactionRepository;
        this.anomalyRepository = anomalyRepository;
        this.highRiskCountries = Set.copyOf(highRiskCountries);
        this.mapper = mapper;
    }

    @Override
    public String name() {
        return "compute_risk_score";
    }

    @Override
    public String description() {
        return "Computes a risk score (0–100) for a transaction based on amount, merchant country,"
                + " and detected anomalies.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties",
                        Map.of(
                                "transaction_id",
                                Map.of("type", "string", "description", "Transaction identifier")),
                "required", List.of("transaction_id"));
    }

    @Override
    public String execute(JsonNode input) {
        TransactionId txId = new TransactionId(input.required("transaction_id").asText());
        Optional<Transaction> txOpt = transactionRepository.findById(txId);
        if (txOpt.isEmpty()) {
            return "{\"error\":\"Transaction not found: " + txId.value() + "\"}";
        }
        Transaction tx = txOpt.get();
        List<Anomaly> anomalies = anomalyRepository.findByTransactionId(txId);

        int score = computeScore(tx, anomalies);
        List<String> factors = buildFactors(tx, anomalies);
        String riskLevel = riskLevel(score);

        ObjectNode result = mapper.createObjectNode();
        result.put("transaction_id", tx.id().value());
        result.put("risk_score", score);
        result.put("risk_level", riskLevel);
        var factorsArray = result.putArray("factors");
        factors.forEach(factorsArray::add);

        try {
            return mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise risk score", e);
        }
    }

    private int computeScore(Transaction tx, List<Anomaly> anomalies) {
        int score = 0;

        BigDecimal amount = tx.amount().value();
        if (amount.compareTo(AMOUNT_CRITICAL) >= 0) score += 50;
        else if (amount.compareTo(AMOUNT_HIGH) >= 0) score += 30;
        else if (amount.compareTo(AMOUNT_MEDIUM) >= 0) score += 15;

        if (highRiskCountries.contains(tx.merchant().country())) score += 20;

        score += Math.min(anomalies.size() * 10, 40);

        boolean hasCritical = anomalies.stream().anyMatch(a -> a.severity() == Severity.CRITICAL);
        if (hasCritical) score += 20;

        return Math.min(score, 100);
    }

    private List<String> buildFactors(Transaction tx, List<Anomaly> anomalies) {
        List<String> factors = new ArrayList<>();
        if (tx.amount().value().compareTo(AMOUNT_HIGH) >= 0) factors.add("high_amount");
        if (highRiskCountries.contains(tx.merchant().country())) factors.add("high_risk_country");
        if (!anomalies.isEmpty()) factors.add("anomalies_detected:" + anomalies.size());
        return factors;
    }

    private static String riskLevel(int score) {
        if (score >= 75) return "CRITICAL";
        if (score >= 50) return "HIGH";
        if (score >= 25) return "MEDIUM";
        return "LOW";
    }
}
