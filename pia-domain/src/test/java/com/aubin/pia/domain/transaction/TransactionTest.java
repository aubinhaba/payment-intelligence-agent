package com.aubin.pia.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.aubin.pia.domain.shared.DomainEvent;

class TransactionTest {

    private static Transaction validTransaction() {
        return Transaction.create(
                new TransactionId("tx_001"),
                Amount.of(new BigDecimal("50.00"), "EUR"),
                new CardReference("hashXYZ", "4242"),
                new Merchant("m_001", "5812", "FR"),
                Instant.parse("2026-04-18T10:00:00Z"),
                TransactionStatus.AUTHORIZED);
    }

    @Test
    void should_create_transaction_with_correct_fields() {
        Transaction tx = validTransaction();
        assertThat(tx.id().value()).isEqualTo("tx_001");
        assertThat(tx.amount().value()).isEqualByComparingTo("50.00");
        assertThat(tx.cardReference().last4()).isEqualTo("4242");
        assertThat(tx.merchant().country()).isEqualTo("FR");
        assertThat(tx.status()).isEqualTo(TransactionStatus.AUTHORIZED);
    }

    @Test
    void should_raise_transaction_ingested_event_on_create() {
        Transaction tx = validTransaction();
        List<DomainEvent> events = tx.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(TransactionIngested.class);
        TransactionIngested event = (TransactionIngested) events.get(0);
        assertThat(event.transactionId().value()).isEqualTo("tx_001");
    }

    @Test
    void should_not_raise_event_on_reconstitute() {
        Transaction tx =
                Transaction.reconstitute(
                        new TransactionId("tx_002"),
                        Amount.of(new BigDecimal("100.00"), "USD"),
                        new CardReference("hashABC", "1234"),
                        new Merchant("m_002", "5411", "US"),
                        Instant.now(),
                        TransactionStatus.DECLINED);
        assertThat(tx.pullDomainEvents()).isEmpty();
    }

    @Test
    void should_clear_events_after_pull() {
        Transaction tx = validTransaction();
        tx.pullDomainEvents();
        assertThat(tx.pullDomainEvents()).isEmpty();
    }

    @Test
    void should_reject_null_id() {
        assertThatThrownBy(
                        () ->
                                Transaction.create(
                                        null,
                                        Amount.of(BigDecimal.ONE, "EUR"),
                                        new CardReference("hash", "4242"),
                                        new Merchant("m_001", "5812", "FR"),
                                        Instant.now(),
                                        TransactionStatus.AUTHORIZED))
                .isInstanceOf(NullPointerException.class);
    }
}
