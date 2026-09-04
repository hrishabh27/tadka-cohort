package com.tadka.api.contracts;

import com.tadka.api.domain.valueobjects.Address;
import com.tadka.api.domain.valueobjects.GeoLocation;
import java.util.List;
import java.util.UUID;

public record RestaurantResponse(
    UUID id,
    String name,
    Address address,
    GeoLocation location,
    boolean isActive,
    List<MenuItemResponse> menuItems
) {}
