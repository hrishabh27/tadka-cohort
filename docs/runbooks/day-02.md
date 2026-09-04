# Day 2 — Runbook: Domain Model & Readiness `/health/ready` (Java Spring Boot)

**Branch:** `day-02` · **What exists today:**
- Rich domain model with 5 bounded contexts: `orders`, `restaurants`, `delivery`, `users`, `payments`.
- Schema-per-domain in PostgreSQL (`orders`, `restaurants`, `delivery`, `users`, `payments`).
- Value Objects: `Money`, `Address`, `GeoLocation` embedded directly into table columns.
- Separation of liveness (`/health`) and readiness (`/health/ready`).

---

## 0. Build and Verify

```bash
# Windows:
.\mvnw.cmd clean test

# macOS / Linux:
./mvnw clean test
```

**Look for:** `BUILD SUCCESS` (3 passed: `TadkaApplicationTests`, `livenessReturnsHealthy`, `readinessReturnsHealthyWithDatabaseCheck`).

---

## 1. Start Postgres

```bash
docker compose up -d postgres
docker compose ps
```

**Look for:** `tadka-postgres` status is `Up ... (healthy)`.

---

## 2. Run the Spring Boot API

Keep this terminal open:

```bash
# Windows:
.\mvnw.cmd spring-boot:run

# macOS / Linux:
./mvnw spring-boot:run
```

---

## 3. Verify Liveness vs Readiness

In a second terminal:

**Liveness (`/health`):**
```bash
curl.exe http://localhost:5224/health
```
Response:
```json
{
  "status": "Healthy",
  "timestamp": "..."
}
```

**Readiness (`/health/ready`):**
```bash
curl.exe http://localhost:5224/health/ready
```
Response:
```json
{
  "status": "healthy",
  "timestamp": "...",
  "checks": {
    "database": {
      "status": "healthy",
      "responseTimeMs": 3
    }
  }
}
```

---

## 4. Verify Schemas & Tables in PostgreSQL

Inspect the database schemas inside the container:

```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\dn"
```
**Look for:** `orders`, `restaurants`, `delivery`, `users`, `payments` (plus `public`).

List tables in each domain:
```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt orders.*"
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt restaurants.*"
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt delivery.*"
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt users.*"
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt payments.*"
```

Verify that Value Objects (`Money`, `Address`) are embedded as columns rather than separate tables:
```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\d restaurants.menu_items"
```
Notice `price_amount` and `price_currency` flattened directly into `menu_items`.

---

## 5. Verify Postgres Down Fails Readiness but Keeps Liveness

```bash
docker compose stop postgres
curl.exe http://localhost:5224/health/ready   # → HTTP 503 Service Unavailable ("unhealthy")
curl.exe http://localhost:5224/health         # → HTTP 200 OK ("Healthy")
```

Restart Postgres:
```bash
docker compose start postgres
# Wait 5s for healthy status, then:
curl.exe http://localhost:5224/health/ready   # → HTTP 200 OK ("healthy")
```
