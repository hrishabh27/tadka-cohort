package com.tadka.api.domain.events;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
