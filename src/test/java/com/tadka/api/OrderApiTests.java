package com.tadka.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tadka.api.contracts.CreateOrderRequest;
import com.tadka.api.contracts.OrderItemRequest;
import com.tadka.api.contracts.UpdateOrderStatusRequest;
import com.tadka.api.domain.orders.OrderStatus;
import com.tadka.api.domain.restaurants.MenuItem;
import com.tadka.api.domain.restaurants.Restaurant;
import com.tadka.api.domain.users.User;
import com.tadka.api.domain.valueobjects.Address;
import com.tadka.api.repositories.MenuItemRepository;
import com.tadka.api.repositories.RestaurantRepository;
import com.tadka.api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getRestaurantsReturnsSeededRestaurants() throws Exception {
        mockMvc.perform(get("/api/v1/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.items[0].name").exists());
    }

    @Test
    void placeOrderCalculatesServerSidePricingAndTransitionsStatus() throws Exception {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        Restaurant restaurant = restaurants.get(0);
        List<MenuItem> menu = menuItemRepository.findByRestaurantId(restaurant.getId());
        MenuItem item1 = menu.get(0);

        List<User> users = userRepository.findAll();
        User user = users.get(0);

        CreateOrderRequest request = new CreateOrderRequest(
                user.getId(),
                restaurant.getId(),
                new Address("Flat 101", "MG Road", "Bengaluru", "560001"),
                List.of(new OrderItemRequest(item1.getId(), 2))
        );

        // 1. Place order
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("Created"))
                .andExpect(jsonPath("$.deliveryFee").value(40.0))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String orderId = objectMapper.readTree(responseBody).get("id").asText();

        // 2. Fetch order
        mockMvc.perform(get("/api/v1/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));

        // 3. Update status: Created -> Confirmed (returns 204 No Content per Day 4 specification)
        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.Confirmed))))
                .andExpect(status().isNoContent());

        // Verify state transitioned in database
        mockMvc.perform(get("/api/v1/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Confirmed"));

        // 4. Invalid transition directly to Delivered or Confirmed->Confirmed should fail with 422
        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(OrderStatus.Confirmed))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Domain Rule Violation"));
    }

    @Test
    void idempotencyKeyReplaysWinnerOrderWithoutDuplicates() throws Exception {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        Restaurant restaurant = restaurants.get(0);
        List<MenuItem> menu = menuItemRepository.findByRestaurantId(restaurant.getId());
        MenuItem item1 = menu.get(0);

        List<User> users = userRepository.findAll();
        User user = users.get(0);

        CreateOrderRequest request = new CreateOrderRequest(
                user.getId(),
                restaurant.getId(),
                new Address("Flat 202", "Indiranagar", "Bengaluru", "560038"),
                List.of(new OrderItemRequest(item1.getId(), 1))
        );

        String idempotencyKey = UUID.randomUUID().toString();

        // First request with idempotency key -> 201 Created
        MvcResult firstResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String firstOrderId = objectMapper.readTree(firstResult.getResponse().getContentAsString()).get("id").asText();

        // Second request with same idempotency key -> 200 OK with same order id
        MvcResult secondResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String secondOrderId = objectMapper.readTree(secondResult.getResponse().getContentAsString()).get("id").asText();
        org.junit.jupiter.api.Assertions.assertEquals(firstOrderId, secondOrderId);
    }
}
