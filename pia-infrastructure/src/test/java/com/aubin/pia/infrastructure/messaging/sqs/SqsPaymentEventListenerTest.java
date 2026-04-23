package com.aubin.pia.infrastructure.messaging.sqs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.application.usecase.IngestTransactionUseCase;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.AmountDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.CardReferenceDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.MerchantDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.MetadataDto;
import com.aubin.pia.infrastructure.messaging.sqs.dto.PaymentEventDto.TransactionDto;

@ExtendWith(MockitoExtension.class)
class SqsPaymentEventListenerTest {

    @Mock private IngestTransactionUseCase ingestTransactionUseCase;
    @Mock private TransactionRepository transactionRepository;
    @Mock private Transaction existingTransaction;

    private SqsPaymentEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SqsPaymentEventListener(ingestTransactionUseCase, transactionRepository);
    }

    @Test
    void handle_valid_event_calls_use_case() {
        when(transactionRepository.findById(any())).thenReturn(Optional.empty());

        listener.handle(buildEvent("tx-new-001"));

        verify(ingestTransactionUseCase).ingest(any());
    }

    @Test
    void handle_duplicate_event_skips_use_case() {
        when(transactionRepository.findById(any())).thenReturn(Optional.of(existingTransaction));

        listener.handle(buildEvent("tx-dup-001"));

        verify(ingestTransactionUseCase, never()).ingest(any());
    }

    private static PaymentEventDto buildEvent(String txId) {
        return new PaymentEventDto(
                "evt-001",
                "PAYMENT_AUTHORIZED",
                Instant.now(),
                new TransactionDto(
                        txId,
                        new AmountDto(new BigDecimal("99.99"), "EUR"),
                        new CardReferenceDto("card-hash-test", "1234"),
                        new MerchantDto("m_test", "5812", "FR"),
                        "AUTHORIZED"),
                new MetadataDto("corr-001"));
    }
}
