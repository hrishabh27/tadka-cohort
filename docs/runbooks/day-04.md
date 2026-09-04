# Day 4 — Runbook: Order Flow Hardening (Idempotency, Optimistic Concurrency, Domain Events)

**Branch:** `day-04` · **What exists today:**
- Idempotency key handling (`Idempotency-Key` HTTP header on `POST /api/v1/orders`) preventing duplicate order creation and replaying identical responses.
- Optimistic Concurrency Control using JPA `@Version` on `Order`: concurrent updates trigger HTTP 409 Conflict with RFC 7807 problem details.
- State Machine validation: invalid state transitions (e.g. `Confirmed` to `Delivered`, or `Confirmed` to `Confirmed`) return HTTP 422 Unprocessable Entity.
- In-process Domain Events: `OrderPlacedEvent` and `OrderConfirmedEvent`. `OrderConfirmedNotificationHandler` dispatches notifications/SMS after commit.

---

## 0. Build and Verify

```bash
# Windows:
.\mvnw.cmd clean test

# macOS / Linux:
./mvnw clean test
```

**Look for:** `BUILD SUCCESS` (6 passed: `TadkaApplicationTests`, `HealthControllerTests`, `OrderApiTests` with idempotency and state transitions).

---

## 1. Verify Idempotency (`Idempotency-Key` Header)

Submit an order with an `Idempotency-Key` header:

```bash
curl.exe -i -X POST http://localhost:5224/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-key-1001" \
  -d '{
    "customerId": "11111111-1111-1111-1111-111111111111",
    "restaurantId": "<RESTAURANT_ID>",
    "items": [{"menuItemId": "<MENU_ITEM_ID>", "quantity": 1}],
    "deliveryAddress": {"line1": "Flat 101", "city": "Bengaluru", "postalCode": "560001"}
  }'
```
**Look for:** HTTP 201 Created on first request.

Re-run the exact same curl command with the same `Idempotency-Key: test-key-1001`:
**Look for:** HTTP 200 OK returning the identical order without inserting a new database record.

---

## 2. Verify Optimistic Concurrency & State Machine (409 vs 422)

### State Machine Transition (204 No Content)
```bash
curl.exe -i -X PATCH http://localhost:5224/api/v1/orders/<ORDER_ID>/status \
  -H "Content-Type: application/json" \
  -d '{"status": "Confirmed"}'
```
**Look for:** HTTP 204 No Content.

In application server logs:
`Notification: order <ORDER_ID> confirmed — SMS sent to customer ...`

### Re-applying Confirmed (422 Unprocessable Entity)
Try to confirm an already-confirmed order:
```bash
curl.exe -i -X PATCH http://localhost:5224/api/v1/orders/<ORDER_ID>/status \
  -H "Content-Type: application/json" \
  -d '{"status": "Confirmed"}'
```
**Look for:** HTTP 422 Unprocessable Entity (`"Cannot transition order from Confirmed to Confirmed: order is already Confirmed"`).

### Illegal Transition (422 Unprocessable Entity)
Try jumping directly to Delivered from Created:
```bash
curl.exe -i -X PATCH http://localhost:5224/api/v1/orders/<ORDER_ID>/status \
  -H "Content-Type: application/json" \
  -d '{"status": "Delivered"}'
```
**Look for:** HTTP 422 Unprocessable Entity (`"Illegal status transition"`).
