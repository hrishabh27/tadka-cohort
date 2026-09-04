package com.tadka.api.contracts;

import com.tadka.api.domain.valueobjects.Address;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    UUID customerId,
    UUID restaurantId,
    Address deliveryAddress,
    List<OrderItemRequest> items
) {}
