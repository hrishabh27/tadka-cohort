package com.tadka.api.domain.orders.events;

import com.tadka.api.domain.events.DomainEvent;
import com.tadka.api.domain.orders.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedEvent(
    UUID orderId,
    OrderStatus status,
    Instant occurredOn
) implements DomainEvent {
    public OrderStatusChangedEvent(UUID orderId, OrderStatus status) {
        this(orderId, status, Instant.now());
    }
}
