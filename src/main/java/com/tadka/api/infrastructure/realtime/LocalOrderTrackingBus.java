package com.tadka.api.infrastructure.realtime;

import com.tadka.api.domain.orders.events.OrderStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Component
public class LocalOrderTrackingBus implements OrderTrackingBus {

    private static final Logger log = LoggerFactory.getLogger(LocalOrderTrackingBus.class);
    private final Map<UUID, List<Consumer<OrderStatusChangedEvent>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void publish(OrderStatusChangedEvent event) {
        List<Consumer<OrderStatusChangedEvent>> listeners = subscribers.get(event.orderId());
        if (listeners != null) {
            log.debug("Dispatching tracking event for order {} to {} subscribers", event.orderId(), listeners.size());
            for (Consumer<OrderStatusChangedEvent> listener : listeners) {
                try {
                    listener.accept(event);
                } catch (Exception ex) {
                    log.error("Error dispatching event to subscriber", ex);
                }
            }
        }
    }

    @Override
    public void subscribe(UUID orderId, Consumer<OrderStatusChangedEvent> listener) {
        subscribers.computeIfAbsent(orderId, id -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public void unsubscribe(UUID orderId, Consumer<OrderStatusChangedEvent> listener) {
        List<Consumer<OrderStatusChangedEvent>> listeners = subscribers.get(orderId);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                subscribers.remove(orderId);
            }
        }
    }
}
