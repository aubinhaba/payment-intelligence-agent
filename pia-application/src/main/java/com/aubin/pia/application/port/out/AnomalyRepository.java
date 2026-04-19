package com.aubin.pia.application.port.out;

import java.util.List;

import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.anomaly.AnomalyType;
import com.aubin.pia.domain.transaction.TransactionId;

public interface AnomalyRepository {
    void save(Anomaly anomaly);

    List<Anomaly> findByTransactionId(TransactionId transactionId);

    List<Anomaly> findSimilar(AnomalyType type, int limit);
}
