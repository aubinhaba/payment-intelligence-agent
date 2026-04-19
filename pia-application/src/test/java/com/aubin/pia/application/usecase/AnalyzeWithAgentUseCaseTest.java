package com.aubin.pia.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aubin.pia.application.port.in.AnalyzeWithAgentCommand;
import com.aubin.pia.application.port.out.AgentPort;
import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class AnalyzeWithAgentUseCaseTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AnomalyRepository anomalyRepository;
    @Mock AgentPort agentPort;
    @Mock MetricsPublisher metricsPublisher;

    @InjectMocks AnalyzeWithAgentUseCase useCase;

    private static final TransactionId TX_ID = new TransactionId("tx_001");

    private static Transaction sampleTransaction() {
        return Transaction.reconstitute(
                TX_ID,
                Amount.of(new BigDecimal("200.00"), "EUR"),
                new CardReference("hash", "9999"),
                new Merchant("m_001", "5812", "FR"),
                Instant.now(),
                TransactionStatus.AUTHORIZED);
    }

    @Test
    void should_call_agent_and_return_content() {
        ReportContent expected = new ReportContent("High risk", "## Details");
        when(transactionRepository.findById(TX_ID)).thenReturn(Optional.of(sampleTransaction()));
        when(anomalyRepository.findByTransactionId(TX_ID)).thenReturn(List.of());
        when(agentPort.analyze(any(Transaction.class), anyList())).thenReturn(expected);

        ReportContent result = useCase.analyze(new AnalyzeWithAgentCommand(TX_ID));

        assertThat(result.summary()).isEqualTo("High risk");
        verify(metricsPublisher).recordAgentLatencyMillis(anyLong());
    }

    @Test
    void should_pass_anomalies_to_agent() {
        when(transactionRepository.findById(TX_ID)).thenReturn(Optional.of(sampleTransaction()));
        List<Anomaly> anomalies = List.of();
        when(anomalyRepository.findByTransactionId(TX_ID)).thenReturn(anomalies);
        when(agentPort.analyze(any(), any())).thenReturn(new ReportContent("ok", "body"));

        useCase.analyze(new AnalyzeWithAgentCommand(TX_ID));

        verify(agentPort).analyze(any(Transaction.class), any());
    }

    @Test
    void should_throw_when_transaction_not_found() {
        when(transactionRepository.findById(TX_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.analyze(new AnalyzeWithAgentCommand(TX_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tx_001");
    }
}
