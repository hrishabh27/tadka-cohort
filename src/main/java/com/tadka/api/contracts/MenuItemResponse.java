package com.tadka.api.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemResponse(
    UUID id,
    UUID restaurantId,
    String name,
    String description,
    BigDecimal price,
    boolean isAvailable
) {}
