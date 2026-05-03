package com.aubin.pia.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aubin.pia.application.usecase.FindTransactionsUseCase;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean FindTransactionsUseCase findTransactionsUseCase;

    @Test
    void get_transactions_returns_200_with_list() throws Exception {
        when(findTransactionsUseCase.findRecent(20)).thenReturn(List.of(sampleTransaction()));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tx_001"))
                .andExpect(jsonPath("$[0].amount").value("150.00"))
                .andExpect(jsonPath("$[0].currency").value("EUR"))
                .andExpect(jsonPath("$[0].merchantId").value("m_001"))
                .andExpect(jsonPath("$[0].status").value("AUTHORIZED"));
    }

    @Test
    void get_transactions_returns_empty_list_when_none() throws Exception {
        when(findTransactionsUseCase.findRecent(20)).thenReturn(List.of());

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void get_transactions_respects_limit_param() throws Exception {
        when(findTransactionsUseCase.findRecent(5)).thenReturn(List.of());

        mockMvc.perform(get("/api/transactions").param("limit", "5")).andExpect(status().isOk());
    }

    private Transaction sampleTransaction() {
        return Transaction.reconstitute(
                new TransactionId("tx_001"),
                new Amount(new BigDecimal("150.00"), Currency.getInstance("EUR")),
                new CardReference("hash_001", "4242"),
                new Merchant("m_001", "5812", "FR"),
                Instant.parse("2026-04-25T10:00:00Z"),
                TransactionStatus.AUTHORIZED);
    }
}
