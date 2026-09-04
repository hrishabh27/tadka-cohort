# Day 3 — Runbook: Monolith REST API, Server-Side Pricing, RFC 7807 & Seeder (Java Spring Boot)

**Branch:** `day-03` · **What exists today:**
- Full Monolith REST API for Restaurants (`/api/v1/restaurants`) and Orders (`/api/v1/orders`).
- Server-side price calculation: client prices are never trusted; prices are resolved directly from database `MenuItem` entities.
- RFC 7807 Problem Details for standard error responses (`NotFoundException`, `DomainException`, validation errors).
- Automatic Demo Data Seeder (`DemoDataSeeder`) initializing restaurants, menus, sample customer, and delivery agents on startup.
- Automated integration test suite covering end-to-end order placement and status transitions.

---

## 0. Build and Verify

```bash
# Windows:
.\mvnw.cmd clean test

# macOS / Linux:
./mvnw clean test
```

**Look for:** `BUILD SUCCESS` (5 passed: `TadkaApplicationTests`, `HealthControllerTests`, `OrderApiTests`).

---

## 1. Start Postgres & Run Application

```bash
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

---

## 2. Explore Seeded Data

### List All Restaurants
```bash
curl.exe -s http://localhost:5224/api/v1/restaurants | jq .
```

### Get Restaurant Details & Menu
```bash
# Pick a restaurant ID from the list, e.g.:
curl.exe -s http://localhost:5224/api/v1/restaurants/{restaurantId} | jq .
curl.exe -s http://localhost:5224/api/v1/restaurants/{restaurantId}/menu | jq .
```

---

## 3. Place an Order (Server-Side Pricing)

Post an order with restaurant ID and menu item IDs. Notice that the client only specifies quantities, never prices:

```bash
curl.exe -X POST http://localhost:5224/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "11111111-1111-1111-1111-111111111111",
    "restaurantId": "<RESTAURANT_ID>",
    "items": [
      {
        "menuItemId": "<MENU_ITEM_ID>",
        "quantity": 2
      }
    ],
    "deliveryAddress": {
      "line1": "Flat 402, Green Meadows",
      "line2": "HSR Layout",
      "city": "Bengaluru",
      "postalCode": "560102"
    }
  }' | jq .
```

**Look for:** Response contains calculated subtotal, 5% tax, fixed 40.00 delivery fee, and total amount.

---

## 4. Verify RFC 7807 Error Responses

Try fetching a non-existent order or restaurant:

```bash
curl.exe -s -i http://localhost:5224/api/v1/orders/00000000-0000-0000-0000-000000000000
```

**Look for:** HTTP 404 with RFC 7807 Problem Detail body:
```json
{
  "type": "https://api.tadka.com/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Order not found: 00000000-0000-0000-0000-000000000000",
  "instance": "/api/v1/orders/00000000-0000-0000-0000-000000000000"
}
```
