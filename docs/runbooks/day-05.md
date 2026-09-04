# Day 5 — Runbook: Database Scaling (Indexes, Connection Pool, Read-Replica Routing & Keyset Pagination)

**Branch:** `day-05` · **What exists today:**
- Database Performance Indexes: `orders(customer_id, created_at DESC)` and `orders(created_at DESC)` eliminating sequential table scans on order history queries.
- HikariCP Connection Pool sizing tuned to min 5, max 50 with explicit timeout configurations.
- Read/Write Splitting with Spring `AbstractRoutingDataSource`: `@Transactional(readOnly = true)` operations route automatically to the read-replica datasource, while writes route to primary.
- Keyset / Cursor Pagination for Order History (`GET /api/v1/orders/history?customerId=...&cursor=...&limit=...`): sub-millisecond query performance regardless of pagination depth.
- Comprehensive integration tests verifying keyset paging, boundary conditions, and 400 error handling for malformed cursors.

---

## 0. Build and Verify

```bash
# Windows:
.\mvnw.cmd clean test

# macOS / Linux:
./mvnw clean test
```

**Look for:** `BUILD SUCCESS` (8 passed: `TadkaApplicationTests`, `HealthControllerTests`, `OrderApiTests` covering order flow, idempotency, keyset pagination, and malformed cursor validation).

---

## 1. Verify Keyset (Cursor) Pagination

### Request First Page
```bash
curl.exe -s "http://localhost:5224/api/v1/orders/history?customerId=<CUSTOMER_ID>&limit=2" | jq .
```
Response:
```json
{
  "items": [...],
  "nextCursor": "<BASE64_CURSOR>",
  "hasMore": true
}
```

### Request Next Page using Cursor
```bash
curl.exe -s "http://localhost:5224/api/v1/orders/history?customerId=<CUSTOMER_ID>&cursor=<BASE64_CURSOR>&limit=2" | jq .
```

### Malformed Cursor Handling (400 Bad Request)
```bash
curl.exe -i "http://localhost:5224/api/v1/orders/history?customerId=<CUSTOMER_ID>&cursor=invalid_cursor"
```
**Look for:** HTTP 400 Bad Request with RFC 7807 problem details.

---

## 2. Verify Composite Performance Indexes in PostgreSQL

```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\d orders.orders"
```
**Look for:**
- `idx_orders_customer_created` on `(customer_id, created_at DESC)`
- `idx_orders_created_at` on `(created_at DESC)`
