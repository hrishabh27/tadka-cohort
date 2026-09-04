package com.tadka.api.controllers;

import com.tadka.api.contracts.*;
import com.tadka.api.domain.orders.IdempotencyKey;
import com.tadka.api.domain.orders.Order;
import com.tadka.api.domain.orders.OrderItem;
import com.tadka.api.domain.orders.OrderStatus;
import com.tadka.api.domain.orders.events.OrderConfirmedEvent;
import com.tadka.api.domain.orders.events.OrderPlacedEvent;
import com.tadka.api.domain.restaurants.MenuItem;
import com.tadka.api.domain.restaurants.Restaurant;
import com.tadka.api.domain.valueobjects.Money;
import com.tadka.api.exceptions.DomainException;
import com.tadka.api.exceptions.NotFoundException;
import com.tadka.api.repositories.IdempotencyKeyRepository;
import com.tadka.api.repositories.MenuItemRepository;
import com.tadka.api.repositories.OrderRepository;
import com.tadka.api.repositories.RestaurantRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrdersController {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrdersController(
            OrderRepository orderRepository,
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateOrderRequest request) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyKey> existing = idempotencyKeyRepository.findByKey(idempotencyKey);
            if (existing.isPresent()) {
                Order order = orderRepository.findById(existing.get().getOrderId())
                        .orElseThrow(() -> new NotFoundException("Order not found: " + existing.get().getOrderId()));
                return ResponseEntity.ok(toResponse(order));
            }
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new DomainException("Order must contain at least one item");
        }

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + request.restaurantId()));

        if (!restaurant.isActive()) {
            throw new DomainException("Restaurant is currently inactive");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemReq : request.items()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.menuItemId())
                    .orElseThrow(() -> new NotFoundException("Menu item not found: " + itemReq.menuItemId()));

            if (!menuItem.getRestaurantId().equals(restaurant.getId())) {
                throw new DomainException("Menu item " + itemReq.menuItemId() + " does not belong to restaurant " + restaurant.getId());
            }

            if (!menuItem.isAvailable()) {
                throw new DomainException("Menu item " + menuItem.getName() + " is currently unavailable");
            }

            if (itemReq.quantity() <= 0) {
                throw new DomainException("Quantity must be at least 1");
            }

            // Price is calculated purely on the server side using the database price
            orderItems.add(new OrderItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), itemReq.quantity()));
        }

        // Server-side pricing: Delivery fee 40 INR, Tax 5% of subtotal
        Money deliveryFee = Money.inr(40.00);
        BigDecimal subtotalValue = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            subtotalValue = subtotalValue.add(item.getTotalPrice().getAmount());
        }
        BigDecimal taxValue = subtotalValue.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        Money tax = Money.inr(taxValue);

        Order order = new Order(
                request.customerId(),
                request.restaurantId(),
                request.deliveryAddress(),
                orderItems,
                deliveryFee,
                tax
        );

        order = orderRepository.save(order);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyKeyRepository.save(new IdempotencyKey(idempotencyKey, order.getId(), HttpStatus.CREATED.value(), order.getId().toString()));
        }

        eventPublisher.publishEvent(new OrderPlacedEvent(order.getId(), order.getCustomerId(), order.getRestaurantId(), order.getTotal()));

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        return ResponseEntity.ok(toResponse(order));
    }

    @GetMapping("/history")
    @Transactional(readOnly = true)
    public ResponseEntity<CursorPageResponse<OrderResponse>> getOrderHistory(
            @RequestParam UUID customerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        int pageSize = Math.max(1, Math.min(limit, 50));

        OrderCursor decodedCursor = OrderCursor.decode(cursor);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, pageSize + 1);

        List<Order> orders;
        if (decodedCursor == null) {
            orders = orderRepository.findFirstPageByCustomerId(customerId, pageable);
        } else {
            orders = orderRepository.findNextPageByCustomerId(customerId, decodedCursor.createdAt(), decodedCursor.id(), pageable);
        }

        boolean hasMore = orders.size() > pageSize;
        List<Order> resultOrders = hasMore ? orders.subList(0, pageSize) : orders;

        String nextCursor = null;
        if (hasMore && !resultOrders.isEmpty()) {
            Order last = resultOrders.get(resultOrders.size() - 1);
            nextCursor = new OrderCursor(last.getCreatedAt(), last.getId()).encode();
        }

        List<OrderResponse> responses = resultOrders.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new CursorPageResponse<>(responses, nextCursor, hasMore));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @RequestBody UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        order.transitionTo(request.status());
        orderRepository.saveAndFlush(order);

        if (request.status() == OrderStatus.Confirmed) {
            eventPublisher.publishEvent(new OrderConfirmedEvent(order.getId(), order.getCustomerId()));
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id, @RequestBody(required = false) CancelOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        order.transitionTo(OrderStatus.Cancelled);
        orderRepository.saveAndFlush(order);

        return ResponseEntity.noContent().build();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(),
                        i.getMenuItemId(),
                        i.getName(),
                        i.getUnitPrice().getAmount(),
                        i.getQuantity(),
                        i.getTotalPrice().getAmount()
                )).toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getStatus(),
                order.getSubtotal().getAmount(),
                order.getDeliveryFee().getAmount(),
                order.getTax().getAmount(),
                order.getTotal().getAmount(),
                order.getDeliveryAddress(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
