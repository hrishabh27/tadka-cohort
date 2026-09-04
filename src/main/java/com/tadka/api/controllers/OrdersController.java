package com.tadka.api.controllers;

import com.tadka.api.contracts.*;
import com.tadka.api.domain.orders.Order;
import com.tadka.api.domain.orders.OrderItem;
import com.tadka.api.domain.orders.OrderStatus;
import com.tadka.api.domain.restaurants.MenuItem;
import com.tadka.api.domain.restaurants.Restaurant;
import com.tadka.api.domain.valueobjects.Money;
import com.tadka.api.exceptions.DomainException;
import com.tadka.api.exceptions.NotFoundException;
import com.tadka.api.repositories.MenuItemRepository;
import com.tadka.api.repositories.OrderRepository;
import com.tadka.api.repositories.RestaurantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrdersController {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public OrdersController(
            OrderRepository orderRepository,
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody CreateOrderRequest request) {
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

        orderRepository.save(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        return ResponseEntity.ok(toResponse(order));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id, @RequestBody UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        OrderStatus current = order.getStatus();
        OrderStatus target = request.status();

        if (!isValidTransition(current, target)) {
            throw new DomainException("Invalid status transition from " + current + " to " + target);
        }

        order.setStatus(target);
        orderRepository.save(order);

        return ResponseEntity.ok(toResponse(order));
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == to) return true;
        return switch (from) {
            case Created -> to == OrderStatus.Confirmed || to == OrderStatus.Cancelled;
            case Confirmed -> to == OrderStatus.Preparing || to == OrderStatus.Cancelled;
            case Preparing -> to == OrderStatus.OutForDelivery || to == OrderStatus.Cancelled;
            case OutForDelivery -> to == OrderStatus.Delivered;
            case Delivered, Cancelled -> false;
        };
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
