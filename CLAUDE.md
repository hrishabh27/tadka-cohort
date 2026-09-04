# Tadka — Code Repo (cohort capstone)

The Java Spring Boot codebase students clone and run. This branch is **Day 1**: monolith scaffold + `/health` + ADR-001/002. It evolves into 4 services + gateway over 16 days. Later `day-NN` branches unlock as the cohort moves.

## Stack (today)

Java 17 · Spring Boot 3.3.3 (Web, JPA) · PostgreSQL 16 · JUnit 5 / Spring Boot Test · Maven (via Maven Wrapper `mvnw`). Redis / Kafka / Gateway / OTEL / Prometheus are later weeks — do not add them on this branch.

## Commands (use PowerShell on Windows or Bash on macOS/Linux)

```bash
# 1. Start PostgreSQL (credentials default to tadka/tadka_local — no .env needed)
docker compose up -d postgres

# 2. Run tests
./mvnw clean test           # macOS/Linux
.\mvnw.cmd clean test       # Windows

# 3. Run application locally
./mvnw spring-boot:run      # macOS/Linux
.\mvnw.cmd spring-boot:run  # Windows

# 4. Check health endpoint (process liveness only, no DB check)
curl http://localhost:5224/health

# Alternatively: run entire stack (Postgres + App) in Docker
docker compose up --build
```

- Compose **database service name is `postgres`** (container `tadka-postgres`).
- `/health` is process-up only. The Day 1 demo adds readiness `/health/ready`, which hits Postgres via Spring Data JPA EntityManager.
- Student demo runbook: `docs/runbooks/day-01.md`.

## Layout

`src/main/java/com/tadka/api` has:
- `TadkaApplication.java` (Spring Boot entrypoint)
- `controllers/HealthController.java` (liveness `/health`)
- `src/main/resources/application.yml` (server port 5224, PostgreSQL datasource)
- `src/test/java/com/tadka/api/TadkaApplicationTests.java` (context load test)

**No extra service projects** and do not pre-create `domain/orders` etc. on this branch.

## Gotchas

- Compose creds default to `tadka/tadka_local`, matching `application.yml` — keep them in sync.
- Always use the Maven wrapper (`./mvnw` or `.\mvnw.cmd`) so no separate Maven installation is required.
- Do not re-introduce empty `k6/`, `terraform/`, `scripts/`, or extra service projects on Day 1.
