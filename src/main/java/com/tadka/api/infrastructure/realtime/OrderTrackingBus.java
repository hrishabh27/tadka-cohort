package com.tadka.api.infrastructure.realtime;

import com.tadka.api.domain.orders.events.OrderStatusChangedEvent;

import java.util.UUID;
import java.util.function.Consumer;

public interface OrderTrackingBus {
    void publish(OrderStatusChangedEvent event);
    void subscribe(UUID orderId, Consumer<OrderStatusChangedEvent> listener);
    void unsubscribe(UUID orderId, Consumer<OrderStatusChangedEvent> listener);
}
