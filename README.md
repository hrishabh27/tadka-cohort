# Tadka 🍛

A food delivery platform, built one earned decision at a time.

This is the code for the [Desi Architect](https://desiarchitect.com) cohort. Tadka starts as a single Java Spring Boot monolith with one endpoint. Over 16 sessions it evolves into four services behind a gateway, with an event backbone, a cache, and full observability.

Nothing here was added because it was fashionable. Every component arrives only after you have watched the system break without it.

---

## Quick Start (Run in under 2 minutes)

This branch is **Day 1**: one Spring Boot API, one PostgreSQL database, and a `/health` endpoint.

### Step 1: Start PostgreSQL
```bash
docker compose up -d postgres
```
*(Wait 5 seconds for `tadka-postgres` to be healthy).*

### Step 2: Run the Spring Boot API
Use the included Maven wrapper (no separate Maven install required):

**Windows (PowerShell/CMD):**
```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
./mvnw spring-boot:run
```

### Step 3: Verify the Health Endpoint
In another terminal:

```bash
curl http://localhost:5224/health
```

Expected response:
```json
{
  "status": "Healthy",
  "timestamp": "2026-09-04T19:26:45.222498100Z"
}
```

> **Note on `/health`**: It is process liveness only (the process is up). It does not hit the database yet. Adding `/health/ready` to check Postgres is part of the Day 1 demo!

---

### Alternative: Run Everything in Docker
If you prefer to run both the database and the API inside Docker containers:

```bash
docker compose up --build
```
The API will be available at `http://localhost:5224/health`.

---

## Prerequisites

| Tool | Version | Verification Command |
|---|---|---|
| [Java JDK](https://adoptium.net/) | 17+ | `java -version` |
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | 4.30+ | `docker compose version` |
| [Git](https://git-scm.com/downloads) | 2.40+ | `git --version` |

*Maven is bundled via the Maven Wrapper (`mvnw` / `mvnw.cmd`), so you do not need Maven installed on your system.*

---

## Running Tests

Run the full automated test suite:

**Windows:**
```powershell
.\mvnw.cmd clean test
```

**macOS / Linux:**
```bash
./mvnw clean test
```

---

## Connection Details

| Component | Host / Port | Details |
|---|---|---|
| **Spring Boot API** | `http://localhost:5224` | Context path `/` |
| **PostgreSQL** | `localhost:5432` | Database: `tadka`, User: `tadka`, Password: `tadka_local` |
| **Compose Service** | `postgres` | Container name: `tadka-postgres` |

Credentials are committed development dummies intended for local testing.

---

## Project Structure

```text
.
├── .mvn/wrapper/        # Maven wrapper configuration & scripts
├── mvnw / mvnw.cmd      # Cross-platform Maven wrapper scripts
├── pom.xml              # Maven dependencies (Spring Boot 3.3.3, Data JPA, PostgreSQL)
├── Dockerfile           # Multi-stage Docker build for API
├── docker-compose.yml   # PostgreSQL 16 & API container definitions
└── src
    ├── main
    │   ├── java/com/tadka/api
    │   │   ├── TadkaApplication.java
    │   │   └── controllers/HealthController.java
    │   └── resources/application.yml
    └── test
        └── java/com/tadka/api/TadkaApplicationTests.java
```

---

## Documentation

- **Setup Guide & Troubleshooting**: [`SETUP.md`](SETUP.md)
- **Day 1 Demo Runbook**: [`docs/runbooks/day-01.md`](docs/runbooks/day-01.md)
- **Architecture Decision Records**: [`docs/adrs/`](docs/adrs/)
