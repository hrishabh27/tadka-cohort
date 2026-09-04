package com.tadka.api.controllers;

import com.tadka.api.contracts.*;
import com.tadka.api.domain.restaurants.MenuItem;
import com.tadka.api.domain.restaurants.Restaurant;
import com.tadka.api.domain.valueobjects.Money;
import com.tadka.api.exceptions.NotFoundException;
import com.tadka.api.repositories.MenuItemRepository;
import com.tadka.api.repositories.RestaurantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantsController {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantsController(RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<RestaurantResponse>> getRestaurants() {
        List<Restaurant> list = restaurantRepository.findByIsActiveTrue();
        List<RestaurantResponse> responses = list.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new PagedResponse<>(responses, responses.size(), 1, responses.size()));
    }

    @GetMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<RestaurantResponse> getRestaurant(@PathVariable UUID id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + id));
        return ResponseEntity.ok(toResponse(restaurant));
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<MenuItemResponse> addMenuItem(@PathVariable UUID id, @RequestBody CreateMenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + id));

        MenuItem item = new MenuItem(
                restaurant.getId(),
                request.name(),
                request.description(),
                Money.inr(request.price()),
                request.isAvailable()
        );

        restaurant.addMenuItem(item);
        restaurantRepository.save(restaurant);

        return ResponseEntity.status(HttpStatus.CREATED).body(toMenuItemResponse(item));
    }

    private RestaurantResponse toResponse(Restaurant r) {
        List<MenuItemResponse> menu = menuItemRepository.findByRestaurantId(r.getId())
                .stream().map(this::toMenuItemResponse).toList();

        return new RestaurantResponse(
                r.getId(),
                r.getName(),
                r.getAddress(),
                r.getLocation(),
                r.isActive(),
                menu
        );
    }

    private MenuItemResponse toMenuItemResponse(MenuItem m) {
        return new MenuItemResponse(
                m.getId(),
                m.getRestaurantId(),
                m.getName(),
                m.getDescription(),
                m.getPrice().getAmount(),
                m.isAvailable()
        );
    }
}
