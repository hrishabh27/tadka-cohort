package com.tadka.api.contracts;

import com.tadka.api.domain.orders.OrderStatus;
import com.tadka.api.domain.valueobjects.Address;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    UUID customerId,
    UUID restaurantId,
    OrderStatus status,
    BigDecimal subtotal,
    BigDecimal deliveryFee,
    BigDecimal tax,
    BigDecimal total,
    Address deliveryAddress,
    List<OrderItemResponse> items,
    Instant createdAt,
    Instant updatedAt
) {}
