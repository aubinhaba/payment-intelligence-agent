package com.aubin.pia.domain.anomaly;

import java.util.Optional;

import com.aubin.pia.domain.transaction.Transaction;

public interface DetectionPolicy {
    Optional<Anomaly> detect(Transaction transaction);
}
