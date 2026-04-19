package com.aubin.pia.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aubin.pia.application.port.in.IngestTransactionCommand;
import com.aubin.pia.application.port.out.EventPublisher;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class IngestTransactionUseCaseTest {

    @Mock TransactionRepository transactionRepository;
    @Mock EventPublisher eventPublisher;
    @Mock MetricsPublisher metricsPublisher;

    @InjectMocks IngestTransactionUseCase useCase;

    private static IngestTransactionCommand validCommand() {
        return new IngestTransactionCommand(
                "tx_001",
                new BigDecimal("49.99"),
                "EUR",
                "sha256hash",
                "4242",
                "m_001",
                "5812",
                "FR",
                "AUTHORIZED",
                Instant.parse("2026-04-18T10:00:00Z"));
    }

    @Test
    void should_persist_transaction() {
        useCase.ingest(validCommand());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void should_publish_domain_events() {
        useCase.ingest(validCommand());
        verify(eventPublisher).publishAll(any());
    }

    @Test
    void should_increment_metrics() {
        useCase.ingest(validCommand());
        verify(metricsPublisher).incrementTransactionsIngested();
    }

    @Test
    void should_return_transaction_with_correct_status() {
        Transaction result = useCase.ingest(validCommand());
        assertThat(result.status()).isEqualTo(TransactionStatus.AUTHORIZED);
        assertThat(result.id().value()).isEqualTo("tx_001");
    }

    @Test
    void should_reject_invalid_status() {
        IngestTransactionCommand bad =
                new IngestTransactionCommand(
                        "tx_002",
                        BigDecimal.ONE,
                        "EUR",
                        "hash",
                        "1234",
                        "m_001",
                        "5812",
                        "FR",
                        "UNKNOWN_STATUS",
                        Instant.now());
        assertThatThrownBy(() -> useCase.ingest(bad)).isInstanceOf(IllegalArgumentException.class);
    }
}
