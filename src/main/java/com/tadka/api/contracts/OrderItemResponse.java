package com.tadka.api.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
    UUID id,
    UUID menuItemId,
    String name,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal totalPrice
) {}
