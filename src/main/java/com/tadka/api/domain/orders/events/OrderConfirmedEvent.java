package com.tadka.api.domain.orders.events;

import com.tadka.api.domain.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderConfirmedEvent(
    UUID orderId,
    UUID customerId,
    Instant occurredOn
) implements DomainEvent {
    public OrderConfirmedEvent(UUID orderId, UUID customerId) {
        this(orderId, customerId, Instant.now());
    }
}
