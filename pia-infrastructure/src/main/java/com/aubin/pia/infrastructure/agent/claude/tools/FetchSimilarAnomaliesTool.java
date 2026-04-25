package com.aubin.pia.infrastructure.agent.claude.tools;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyType;

public class FetchSimilarAnomaliesTool implements AgentTool {

    private final AnomalyRepository anomalyRepository;
    private final ObjectMapper mapper;

    public FetchSimilarAnomaliesTool(AnomalyRepository anomalyRepository, ObjectMapper mapper) {
        this.anomalyRepository = anomalyRepository;
        this.mapper = mapper;
    }

    @Override
    public String name() {
        return "fetch_similar_anomalies";
    }

    @Override
    public String description() {
        return "Fetches recent historical anomalies of a given type, providing context for pattern"
                + " comparison.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "properties",
                        Map.of(
                                "anomaly_type",
                                Map.of(
                                        "type",
                                        "string",
                                        "enum",
                                        List.of("VELOCITY", "AMOUNT", "GEO", "CARD_TESTING"),
                                        "description",
                                        "Type of anomaly to search for"),
                                "limit",
                                Map.of(
                                        "type",
                                        "integer",
                                        "description",
                                        "Maximum number of results to return")),
                "required", List.of("anomaly_type", "limit"));
    }

    @Override
    public String execute(JsonNode input) {
        AnomalyType type = AnomalyType.valueOf(input.required("anomaly_type").asText());
        int limit = input.required("limit").asInt();
        List<Anomaly> anomalies = anomalyRepository.findSimilar(type, limit);

        ArrayNode result = mapper.createArrayNode();
        for (Anomaly a : anomalies) {
            result.addObject()
                    .put("id", a.id().value())
                    .put("transaction_id", a.transactionId().value())
                    .put("type", a.type().name())
                    .put("severity", a.severity().name())
                    .put("description", a.description())
                    .put("detected_at", a.detectedAt().toString());
        }
        try {
            return mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise similar anomalies", e);
        }
    }
}
