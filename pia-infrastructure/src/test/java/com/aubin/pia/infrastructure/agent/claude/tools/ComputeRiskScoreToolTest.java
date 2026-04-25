package com.aubin.pia.infrastructure.agent.claude.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyId;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.anomaly.Severity;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class ComputeRiskScoreToolTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AnomalyRepository anomalyRepository;

    private ComputeRiskScoreTool tool;
    private ObjectMapper mapper;

    private static final Set<String> HIGH_RISK_COUNTRIES =
            Set.of("AE", "MT", "SG", "HK", "RU", "NG");

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        tool =
                new ComputeRiskScoreTool(
                        transactionRepository, anomalyRepository, HIGH_RISK_COUNTRIES, mapper);
    }

    @Test
    void name_returns_compute_risk_score() {
        assertThat(tool.name()).isEqualTo("compute_risk_score");
    }

    @Test
    void execute_returns_low_score_for_normal_transaction_in_safe_country() throws Exception {
        TransactionId txId = new TransactionId("tx_low");
        when(transactionRepository.findById(txId))
                .thenReturn(Optional.of(tx(txId, "200.00", "FR")));
        when(anomalyRepository.findByTransactionId(txId)).thenReturn(List.of());

        JsonNode input = mapper.createObjectNode().put("transaction_id", "tx_low");
        String result = tool.execute(input);
        JsonNode json = mapper.readTree(result);

        assertThat(json.get("transaction_id").asText()).isEqualTo("tx_low");
        assertThat(json.get("risk_score").asInt()).isLessThan(30);
        assertThat(json.get("risk_level").asText()).isNotBlank();
    }

    @Test
    void execute_returns_high_score_for_large_amount_in_high_risk_country_with_critical_anomaly()
            throws Exception {
        TransactionId txId = new TransactionId("tx_high");
        when(transactionRepository.findById(txId))
                .thenReturn(Optional.of(tx(txId, "15000.00", "AE")));
        when(anomalyRepository.findByTransactionId(txId))
                .thenReturn(List.of(anomaly(txId, Severity.CRITICAL)));

        JsonNode input = mapper.createObjectNode().put("transaction_id", "tx_high");
        String result = tool.execute(input);
        JsonNode json = mapper.readTree(result);

        assertThat(json.get("risk_score").asInt()).isGreaterThan(70);
    }

    @Test
    void execute_score_never_exceeds_100() throws Exception {
        TransactionId txId = new TransactionId("tx_max");
        when(transactionRepository.findById(txId))
                .thenReturn(Optional.of(tx(txId, "50000.00", "RU")));
        when(anomalyRepository.findByTransactionId(txId))
                .thenReturn(
                        List.of(
                                anomaly(txId, Severity.CRITICAL),
                                anomaly(txId, Severity.CRITICAL),
                                anomaly(txId, Severity.HIGH),
                                anomaly(txId, Severity.HIGH),
                                anomaly(txId, Severity.MEDIUM)));

        JsonNode input = mapper.createObjectNode().put("transaction_id", "tx_max");
        String result = tool.execute(input);
        JsonNode json = mapper.readTree(result);

        assertThat(json.get("risk_score").asInt()).isLessThanOrEqualTo(100);
    }

    @Test
    void execute_adds_geo_factor_for_high_risk_country() throws Exception {
        TransactionId safeId = new TransactionId("tx_safe");
        TransactionId riskId = new TransactionId("tx_risk");
        BigDecimal sameAmount = new BigDecimal("200.00");

        when(transactionRepository.findById(safeId))
                .thenReturn(Optional.of(tx(safeId, "200.00", "FR")));
        when(transactionRepository.findById(riskId))
                .thenReturn(Optional.of(tx(riskId, "200.00", "NG")));
        when(anomalyRepository.findByTransactionId(safeId)).thenReturn(List.of());
        when(anomalyRepository.findByTransactionId(riskId)).thenReturn(List.of());

        int safeScore =
                mapper.readTree(
                                tool.execute(
                                        mapper.createObjectNode().put("transaction_id", "tx_safe")))
                        .get("risk_score")
                        .asInt();
        int riskScore =
                mapper.readTree(
                                tool.execute(
                                        mapper.createObjectNode().put("transaction_id", "tx_risk")))
                        .get("risk_score")
                        .asInt();

        assertThat(riskScore).isGreaterThan(safeScore);
    }

    private static Transaction tx(TransactionId id, String amount, String country) {
        return Transaction.reconstitute(
                id,
                Amount.of(new BigDecimal(amount), "EUR"),
                new CardReference("hash_001", "4242"),
                new Merchant("m_001", "5812", country),
                Instant.now(),
                TransactionStatus.AUTHORIZED);
    }

    private static Anomaly anomaly(TransactionId txId, Severity severity) {
        return Anomaly.reconstitute(
                new AnomalyId(UUID.randomUUID().toString()),
                txId,
                AnomalyType.AMOUNT,
                severity,
                "Test anomaly",
                Instant.now());
    }
}
