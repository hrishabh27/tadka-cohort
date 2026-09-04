# Day 1 — Runbook: Scaffold + `/health` (Java Spring Boot)

**Branch:** `day-01` · **What exists today:** one Spring Boot API, one PostgreSQL database, a liveness `/health` that does **not** talk to Postgres. Nothing else — and that is the point.

Compose commands and service vs container name: [`docs/learn/docker.md`](../learn/docker.md).

> **Windows PowerShell:** `curl` is an alias for `Invoke-WebRequest`. Use **`curl.exe`** everywhere below (or Git Bash / WSL / `Invoke-RestMethod`).

| Thing | Value |
|---|---|
| API (http) | `http://localhost:5224` |
| Compose service | `postgres` (container `tadka-postgres`) |
| Postgres | `localhost:5432`, db `tadka`, user `tadka`, password `tadka_local` |

---

## 0. Build and Verify Baseline

```bash
# Windows:
.\mvnw.cmd clean test

# macOS / Linux:
./mvnw clean test
```

**Look for:** `BUILD SUCCESS` (0 failures, 0 errors).

---

## 1. Start Postgres

```bash
docker compose up -d postgres
docker compose ps
```

**Look for:**
```
NAME             IMAGE         STATUS
tadka-postgres   postgres:16   Up ... (healthy)
```

---

## 2. Run the API

Keep this terminal open:

```bash
# Windows:
.\mvnw.cmd spring-boot:run

# macOS / Linux:
./mvnw spring-boot:run
```

**Look for:**
```
Tomcat started on port 5224 (http) with context path '/'
Started TadkaApplication in ...
```

---

## 3. Baseline — Liveness Only

In a **second** terminal, from the repo root:

```bash
curl.exe -s -w "\nHTTP %{http_code}\n" http://localhost:5224/health
```

**Look for — HTTP 200:**
```json
{ "status": "Healthy", "timestamp": "2026-..." }
```

Notice what is **not** there: `database` or `responseTime`. This endpoint only confirms the process is alive.

---

## 4. Live Build — Add `/health/ready`

This is the Day 1 demo. Open Copilot Chat in **Agent Mode**, and prompt:

```
Add a /health/ready endpoint to HealthController that checks PostgreSQL connectivity.
It should attempt a simple query (e.g. using DataSource or EntityManager), measure the response time, and return:
- database status (healthy/unhealthy)
- response time in milliseconds
- overall status based on all checks

Return 200 if healthy, 503 if the database check fails.
```

Restart the API after changes are made.

---

## 5. Ready Check — Postgres Up

```bash
curl.exe -s -w "\nHTTP %{http_code}\n" http://localhost:5224/health/ready
```

**Look for — HTTP 200:**
```json
{
  "status": "healthy",
  "timestamp": "...",
  "checks": {
    "database": { "status": "healthy", "responseTimeMs": 3 }
  }
}
```

---

## 6. Break — Stop Postgres

```bash
docker compose stop postgres
curl.exe -s -w "\nHTTP %{http_code}\n" http://localhost:5224/health/ready
curl.exe -s -w "\nHTTP %{http_code}\n" http://localhost:5224/health
```

**Expected:**
- `/health/ready` returns **HTTP 503** (unhealthy).
- `/health` returns **HTTP 200** (process is still up).

This demonstrates why liveness (`/health`) and readiness (`/health/ready`) must be separated!

---

## 7. Fix — Start Postgres

```bash
docker compose start postgres
docker compose ps
# Wait until tadka-postgres is (healthy), then:
curl.exe -s -w "\nHTTP %{http_code}\n" http://localhost:5224/health/ready
```

**Look for:** HTTP 200 again once Postgres is healthy.
