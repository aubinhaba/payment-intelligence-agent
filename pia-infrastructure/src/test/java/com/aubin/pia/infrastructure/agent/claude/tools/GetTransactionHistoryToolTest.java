package com.aubin.pia.infrastructure.agent.claude.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class GetTransactionHistoryToolTest {

    @Mock TransactionRepository transactionRepository;

    private GetTransactionHistoryTool tool;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        tool = new GetTransactionHistoryTool(transactionRepository, mapper);
    }

    @Test
    void name_returns_get_transaction_history() {
        assertThat(tool.name()).isEqualTo("get_transaction_history");
    }

    @Test
    void execute_returns_json_array_for_card() throws Exception {
        String cardHash = "hash_001";
        when(transactionRepository.findByCardReference(cardHash, 24))
                .thenReturn(List.of(sampleTransaction(cardHash)));

        JsonNode input =
                mapper.createObjectNode()
                        .put("card_reference_hash", cardHash)
                        .put("window_hours", 24);

        String result = tool.execute(input);
        JsonNode json = mapper.readTree(result);

        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(1);
        assertThat(json.get(0).get("id").asText()).isEqualTo("tx_hist_001");
        assertThat(json.get(0).get("amount").asText()).isEqualTo("120.00");
    }

    @Test
    void execute_returns_empty_array_when_no_transactions() throws Exception {
        when(transactionRepository.findByCardReference("unknown", 1)).thenReturn(List.of());

        JsonNode input =
                mapper.createObjectNode()
                        .put("card_reference_hash", "unknown")
                        .put("window_hours", 1);

        String result = tool.execute(input);
        JsonNode json = mapper.readTree(result);

        assertThat(json.isArray()).isTrue();
        assertThat(json.isEmpty()).isTrue();
    }

    @Test
    void input_schema_declares_required_fields() {
        var schema = tool.inputSchema();
        assertThat(schema).containsKey("required");
    }

    private static Transaction sampleTransaction(String cardHash) {
        return Transaction.reconstitute(
                new TransactionId("tx_hist_001"),
                Amount.of(new BigDecimal("120.00"), "EUR"),
                new CardReference(cardHash, "4242"),
                new Merchant("m_001", "5812", "FR"),
                Instant.now(),
                TransactionStatus.AUTHORIZED);
    }
}
