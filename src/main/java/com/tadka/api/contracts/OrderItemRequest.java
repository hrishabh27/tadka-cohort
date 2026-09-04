package com.tadka.api.contracts;

import java.util.UUID;

public record OrderItemRequest(
    UUID menuItemId,
    int quantity
) {}
