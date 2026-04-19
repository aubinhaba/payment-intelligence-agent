package com.aubin.pia.application.port.out;

import java.util.List;

import com.aubin.pia.domain.shared.DomainEvent;

public interface EventPublisher {
    void publishAll(List<DomainEvent> events);
}
