package com.tadka.api.controllers;

import com.tadka.api.domain.orders.Order;
import com.tadka.api.domain.orders.events.OrderStatusChangedEvent;
import com.tadka.api.exceptions.NotFoundException;
import com.tadka.api.infrastructure.realtime.OrderTrackingBus;
import com.tadka.api.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderTrackingController {

    private static final Logger log = LoggerFactory.getLogger(OrderTrackingController.class);

    private final OrderRepository orderRepository;
    private final OrderTrackingBus trackingBus;

    public OrderTrackingController(OrderRepository orderRepository, OrderTrackingBus trackingBus) {
        this.orderRepository = orderRepository;
        this.trackingBus = trackingBus;
    }

    @GetMapping(value = {"/{id}/events", "/{id}/tracking/stream"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter trackOrder(@PathVariable UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        // 5 minute timeout for SSE stream
        SseEmitter emitter = new SseEmitter(300_000L);

        Consumer<OrderStatusChangedEvent> listener = event -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(event));
            } catch (IOException e) {
                log.warn("Failed to send SSE event to client for order {}: {}", id, e.getMessage());
                emitter.completeWithError(e);
            }
        };

        trackingBus.subscribe(id, listener);

        Runnable cleanup = () -> trackingBus.unsubscribe(id, listener);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // Emit current initial status immediately upon connecting
        try {
            emitter.send(SseEmitter.event()
                    .name("status")
                    .data(new OrderStatusChangedEvent(order.getId(), order.getStatus(), order.getUpdatedAt())));
        } catch (IOException e) {
            log.warn("Failed to emit initial SSE state for order {}: {}", id, e.getMessage());
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
