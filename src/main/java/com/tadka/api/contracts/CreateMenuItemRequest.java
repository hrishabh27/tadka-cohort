package com.tadka.api.contracts;

import java.math.BigDecimal;

public record CreateMenuItemRequest(
    String name,
    String description,
    BigDecimal price,
    boolean isAvailable
) {}
