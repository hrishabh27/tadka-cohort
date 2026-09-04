package com.tadka.api.contracts;

import com.tadka.api.domain.orders.OrderStatus;

public record UpdateOrderStatusRequest(
    OrderStatus status
) {}
