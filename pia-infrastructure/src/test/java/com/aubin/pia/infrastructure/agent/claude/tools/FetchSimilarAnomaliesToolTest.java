package com.aubin.pia.infrastructure.agent.claude.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.TransactionId;

@ExtendWith(MockitoExtension.class)
class FetchSimilarAnomaliesToolTest {

    @Mock AnomalyRepository anomalyRepository;

    private FetchSimilarAnomaliesTool tool;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        tool = new FetchSimilarAnomaliesTool(anomalyRepository, mapper);
    }

    @Test
    void name_returns_fetch_similar_anomalies() {
        assertThat(tool.name()).isEqualTo("fetch_similar_anomalies");
    }

    @Test
    void execute_returns_anomaly_list_with_correct_fields() throws Exception {
        when(anomalyRepository.findSimilar(AnomalyType.VELOCITY, 5))
                .thenReturn(List.of(anomaly(AnomalyType.VELOCITY, Severity.HIGH)));

        JsonNode input = mapper.createObjectNode().put("anomaly_type", "VELOCITY").put("limit", 5);

        String result = tool.execute(input);
        JsonNode json = mapper.readTree(result);

        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(1);
        assertThat(json.get(0).get("type").asText()).isEqualTo("VELOCITY");
        assertThat(json.get(0).get("severity").asText()).isEqualTo("HIGH");
    }

    @Test
    void execute_returns_empty_array_when_no_similar_anomalies() throws Exception {
        when(anomalyRepository.findSimilar(AnomalyType.GEO, 10)).thenReturn(List.of());

        JsonNode input = mapper.createObjectNode().put("anomaly_type", "GEO").put("limit", 10);

        String result = tool.execute(input);
        JsonNode json = mapper.readTree(result);

        assertThat(json.isArray()).isTrue();
        assertThat(json.isEmpty()).isTrue();
    }

    @Test
    void execute_supports_all_anomaly_types() throws Exception {
        for (AnomalyType type : AnomalyType.values()) {
            when(anomalyRepository.findSimilar(type, 1))
                    .thenReturn(List.of(anomaly(type, Severity.MEDIUM)));

            JsonNode input =
                    mapper.createObjectNode().put("anomaly_type", type.name()).put("limit", 1);

            String result = tool.execute(input);
            assertThat(mapper.readTree(result).isArray())
                    .as("Expected array for type %s", type)
                    .isTrue();
        }
    }

    private static Anomaly anomaly(AnomalyType type, Severity severity) {
        return Anomaly.reconstitute(
                new AnomalyId(UUID.randomUUID().toString()),
                new TransactionId("tx_001"),
                type,
                severity,
                "Test anomaly description",
                Instant.now());
    }
}
