package com.tadka.api.domain.orders.events;

import com.tadka.api.domain.events.DomainEvent;
import com.tadka.api.domain.valueobjects.Money;

import java.time.Instant;
import java.util.UUID;

public record OrderPlacedEvent(
    UUID orderId,
    UUID customerId,
    UUID restaurantId,
    Money totalAmount,
    Instant occurredOn
) implements DomainEvent {
    public OrderPlacedEvent(UUID orderId, UUID customerId, UUID restaurantId, Money totalAmount) {
        this(orderId, customerId, restaurantId, totalAmount, Instant.now());
    }
}
