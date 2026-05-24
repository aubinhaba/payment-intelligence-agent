package com.aubin.pia.application.usecase;

import java.util.List;

import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.domain.transaction.Transaction;

public class FindTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public FindTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> findRecent(int limit) {
        return transactionRepository.findRecent(limit);
    }

    public List<Transaction> findPaged(int page, int size) {
        return transactionRepository.findPaged(page, size);
    }
}
