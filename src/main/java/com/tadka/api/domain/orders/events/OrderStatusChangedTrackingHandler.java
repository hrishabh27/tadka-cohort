package com.tadka.api.domain.orders.events;

import com.tadka.api.infrastructure.realtime.OrderTrackingBus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusChangedTrackingHandler {

    private final OrderTrackingBus trackingBus;

    public OrderStatusChangedTrackingHandler(OrderTrackingBus trackingBus) {
        this.trackingBus = trackingBus;
    }

    @EventListener
    public void handle(OrderStatusChangedEvent event) {
        trackingBus.publish(event);
    }
}
