package com.aubin.pia.application.usecase;

import java.util.List;

import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.domain.anomaly.Anomaly;

public class FindAnomaliesUseCase {

    private final AnomalyRepository anomalyRepository;

    public FindAnomaliesUseCase(AnomalyRepository anomalyRepository) {
        this.anomalyRepository = anomalyRepository;
    }

    public List<Anomaly> findRecent(int limit) {
        return anomalyRepository.findRecent(limit);
    }

    public List<Anomaly> findPaged(int page, int size) {
        return anomalyRepository.findPaged(page, size);
    }
}
