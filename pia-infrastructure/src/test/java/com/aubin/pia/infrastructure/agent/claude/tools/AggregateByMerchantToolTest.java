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
class AggregateByMerchantToolTest {

    @Mock TransactionRepository transactionRepository;

    private AggregateByMerchantTool tool;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        tool = new AggregateByMerchantTool(transactionRepository, mapper);
    }

    @Test
    void name_returns_aggregate_by_merchant() {
        assertThat(tool.name()).isEqualTo("aggregate_by_merchant");
    }

    @Test
    void execute_returns_aggregation_for_merchant_with_transactions() throws Exception {
        String merchantId = "m_001";
        when(transactionRepository.findByMerchantId(merchantId, 24))
                .thenReturn(
                        List.of(
                                transaction(merchantId, "100.00"),
                                transaction(merchantId, "200.00"),
                                transaction(merchantId, "300.00")));

        JsonNode input =
                mapper.createObjectNode().put("merchant_id", merchantId).put("window_hours", 24);

        String result = tool.execute(input);
        JsonNode json = mapper.readTree(result);

        assertThat(json.get("merchant_id").asText()).isEqualTo(merchantId);
        assertThat(json.get("transaction_count").asInt()).isEqualTo(3);
        assertThat(new BigDecimal(json.get("total_amount").asText()))
                .isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(new BigDecimal(json.get("average_amount").asText()))
                .isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void execute_returns_zeros_when_no_transactions() throws Exception {
        when(transactionRepository.findByMerchantId("m_empty", 1)).thenReturn(List.of());

        JsonNode input =
                mapper.createObjectNode().put("merchant_id", "m_empty").put("window_hours", 1);

        String result = tool.execute(input);
        JsonNode json = mapper.readTree(result);

        assertThat(json.get("transaction_count").asInt()).isEqualTo(0);
        assertThat(json.get("total_amount").asText()).isEqualTo("0.00");
        assertThat(json.get("average_amount").asText()).isEqualTo("0.00");
    }

    private static Transaction transaction(String merchantId, String amount) {
        return Transaction.reconstitute(
                new TransactionId("tx_" + amount.replace(".", "_")),
                Amount.of(new BigDecimal(amount), "EUR"),
                new CardReference("hash_001", "4242"),
                new Merchant(merchantId, "5812", "FR"),
                Instant.now(),
                TransactionStatus.AUTHORIZED);
    }
}
