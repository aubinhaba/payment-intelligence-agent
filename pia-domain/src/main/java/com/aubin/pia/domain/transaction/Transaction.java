package com.aubin.pia.domain.transaction;

import java.time.Instant;
import java.util.Objects;

import com.aubin.pia.domain.shared.AggregateRoot;

public class Transaction extends AggregateRoot {
    private final TransactionId id;
    private final Amount amount;
    private final CardReference cardReference;
    private final Merchant merchant;
    private final Instant occurredAt;
    private TransactionStatus status;

    private Transaction(
            TransactionId id,
            Amount amount,
            CardReference cardReference,
            Merchant merchant,
            Instant occurredAt,
            TransactionStatus status) {
        this.id = id;
        this.amount = amount;
        this.cardReference = cardReference;
        this.merchant = merchant;
        this.occurredAt = occurredAt;
        this.status = status;
    }

    /** Creates a new transaction and raises a {@link TransactionIngested} domain event. */
    public static Transaction create(
            TransactionId id,
            Amount amount,
            CardReference cardReference,
            Merchant merchant,
            Instant occurredAt,
            TransactionStatus status) {
        validateNotNull(id, amount, cardReference, merchant, occurredAt, status);
        Transaction tx = new Transaction(id, amount, cardReference, merchant, occurredAt, status);
        tx.registerEvent(new TransactionIngested(id));
        return tx;
    }

    /** Rebuilds a transaction from persistence without raising domain events. */
    public static Transaction reconstitute(
            TransactionId id,
            Amount amount,
            CardReference cardReference,
            Merchant merchant,
            Instant occurredAt,
            TransactionStatus status) {
        validateNotNull(id, amount, cardReference, merchant, occurredAt, status);
        return new Transaction(id, amount, cardReference, merchant, occurredAt, status);
    }

    public TransactionId id() {
        return id;
    }

    public Amount amount() {
        return amount;
    }

    public CardReference cardReference() {
        return cardReference;
    }

    public Merchant merchant() {
        return merchant;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    public TransactionStatus status() {
        return status;
    }

    private static void validateNotNull(
            TransactionId id,
            Amount amount,
            CardReference cardReference,
            Merchant merchant,
            Instant occurredAt,
            TransactionStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(cardReference, "cardReference must not be null");
        Objects.requireNonNull(merchant, "merchant must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}
