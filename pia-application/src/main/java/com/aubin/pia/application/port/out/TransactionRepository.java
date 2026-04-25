package com.aubin.pia.application.port.out;

import java.util.List;
import java.util.Optional;

import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.domain.transaction.TransactionId;

public interface TransactionRepository {
    void save(Transaction transaction);

    Optional<Transaction> findById(TransactionId id);

    List<Transaction> findByCardReference(String cardHash, int windowHours);

    List<Transaction> findByMerchantId(String merchantId, int windowHours);
}
