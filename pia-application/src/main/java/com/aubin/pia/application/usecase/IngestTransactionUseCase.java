package com.aubin.pia.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aubin.pia.application.port.in.IngestTransactionCommand;
import com.aubin.pia.application.port.out.EventPublisher;
import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.transaction.Amount;
import com.aubin.pia.domain.transaction.CardReference;
import com.aubin.pia.domain.transaction.Merchant;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;
import com.aubin.pia.domain.transaction.TransactionStatus;

public class IngestTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(IngestTransactionUseCase.class);

    private final TransactionRepository transactionRepository;
    private final EventPublisher eventPublisher;
    private final MetricsPublisher metricsPublisher;

    public IngestTransactionUseCase(
            TransactionRepository transactionRepository,
            EventPublisher eventPublisher,
            MetricsPublisher metricsPublisher) {
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
        this.metricsPublisher = metricsPublisher;
    }

    public Transaction ingest(IngestTransactionCommand command) {
        log.debug(
                "ingest.start txId={} amount={} {} merchant={}",
                command.transactionId(),
                command.amount(),
                command.currency(),
                command.merchantId());

        Transaction transaction =
                Transaction.create(
                        new TransactionId(command.transactionId()),
                        Amount.of(command.amount(), command.currency()),
                        new CardReference(command.cardHash(), command.cardLast4()),
                        new Merchant(
                                command.merchantId(),
                                command.merchantMcc(),
                                command.merchantCountry()),
                        command.occurredAt(),
                        TransactionStatus.valueOf(command.status()));
        transactionRepository.save(transaction);
        eventPublisher.publishAll(transaction.pullDomainEvents());
        metricsPublisher.incrementTransactionsIngested();

        log.info("ingest.done txId={}", command.transactionId());
        return transaction;
    }
}
