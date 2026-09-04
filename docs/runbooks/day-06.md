# Day 6 — Runbook: Scale-Out, Caching, Realtime SSE & Rate Limiting

**Branch:** `day-06` · **What exists today:**
- Cache-Aside service with single-flight mutex lock preventing cache stampedes/thundering herd.
- Restaurant menu caching (`GET /api/v1/restaurants/{id}/menu`) with a 60-second TTL and automatic delete-on-write cache invalidation when new items are added (`POST /api/v1/restaurants/{id}/menu`).
- Real-time Order Tracking via Server-Sent Events (SSE) (`GET /api/v1/orders/{id}/events`): clients stream order status updates live as transitions occur (`Created` → `Confirmed` → `Preparing` → `OutForDelivery` → `Delivered`).
- Fixed-window Rate Limiting Filter: throttles client IP traffic beyond 100 req/min, returning HTTP 429 Too Many Requests with RFC 7807 problem details.
- Redis container definition added to `docker-compose.yml` with transparent fallback for standalone operation and automated testing.

---

## 0. Build and Verify

```bash
# Windows:
.\mvnw.cmd clean test

# macOS / Linux:
./mvnw clean test
```

**Look for:** `BUILD SUCCESS` (10 passed: `TadkaApplicationTests`, `HealthControllerTests`, `OrderApiTests` covering order lifecycle, idempotency, keyset pagination, cache-aside invalidation, and SSE order tracking).

---

## 1. Verify Cache-Aside & Invalidation

### Fetch Menu (Cache Miss → Populates Cache)
```bash
curl.exe -s http://localhost:5224/api/v1/restaurants/<RESTAURANT_ID>/menu | jq .
```

### Fetch Menu Again (Cache Hit)
Subsequent requests within 60 seconds are served directly from cache without hitting the database.

### Add New Menu Item (Cache Invalidation)
```bash
curl.exe -i -X POST http://localhost:5224/api/v1/restaurants/<RESTAURANT_ID>/menu \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tadka Special Dal",
    "description": "Slow cooked black lentils with butter and cream",
    "price": 280.00,
    "isAvailable": true
  }'
```
**Look for:** HTTP 201 Created. The cache key `restaurant:menu:<RESTAURANT_ID>` is immediately evicted.

### Next Fetch (Cache Repopulation)
The next GET call hits the database and caches the freshly updated menu list with the new item included.

---

## 2. Verify Realtime Order Tracking via Server-Sent Events (SSE)

### Start Streaming Order Events in Terminal A
```bash
curl.exe -N -H "Accept: text/event-stream" http://localhost:5224/api/v1/orders/<ORDER_ID>/events
```
Terminal A immediately prints the initial order status event:
```
event:status
data:{"orderId":"<ORDER_ID>","status":"Created","occurredOn":"..."}
```

### Update Order Status in Terminal B
```bash
curl.exe -i -X PATCH http://localhost:5224/api/v1/orders/<ORDER_ID>/status \
  -H "Content-Type: application/json" \
  -d '{"status": "Confirmed"}'
```

Terminal A immediately streams the status transition:
```
event:status
data:{"orderId":"<ORDER_ID>","status":"Confirmed","occurredOn":"..."}
```

---

## 3. Verify Rate Limiting (429 Too Many Requests)

When a client IP exceeds 100 requests per minute, the server responds with HTTP 429:
```json
{
  "type": "https://tadka.com/errors/rate-limit-exceeded",
  "title": "Too Many Requests",
  "status": 429,
  "detail": "Rate limit exceeded. Maximum 100 requests per minute."
}
```
