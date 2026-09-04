# Setup Guide - Desi Architect Cohort (Java Edition)

Get everything installed before Day 1. This should take 15–20 minutes. If something doesn't work, post in `#doubts` on Discord with a screenshot of the error.

**Local database password:** Compose and `application.yml` default to `tadka` / `tadka_local`. That dummy is committed so you can clone and run without needing a `.env` file. It is a throwaway local Postgres, not a secret.

---

## Required Tools

### 1. Docker Desktop

We use Docker to run infrastructure locally. **Day 1 is PostgreSQL only.** Later weeks add more containers to the same compose file.

- **Version:** Docker Desktop 4.30+ (Docker Engine 26+)
- **Install:**
  - Windows: https://docs.docker.com/desktop/install/windows-install/
  - Mac: https://docs.docker.com/desktop/install/mac-install/
  - Linux: https://docs.docker.com/desktop/install/linux-install/

**Verify:**
```bash
docker --version
docker compose version
```

*Important:* On Windows, enable the WSL 2 backend (Docker Desktop → Settings → General → Use WSL 2 based engine).

---

### 2. Java 17+ JDK

The Tadka backend is built with Java 17 and Spring Boot 3.

- **Version:** Java 17 LTS (Eclipse Temurin recommended)
- **Install:** https://adoptium.net/temurin/releases/?version=17

**Verify:**
```bash
java -version
javac -version
# Expected: openjdk version "17.x.x"
```

---

### 3. IDE / Editor

VS Code or IntelliJ IDEA Community/Ultimate.

**If using VS Code:**
Install the **Extension Pack for Java**:
```bash
code --install-extension vscjava.vscode-java-pack
```

Optional extensions:
- Docker: `ms-azuretools.vscode-docker`
- REST Client: `humao.rest-client`
- GitHub Copilot: `github.copilot`

---

### 4. Git

- **Version:** 2.40+
- **Install:** https://git-scm.com/downloads

---

## Verify Everything Works

Run these commands one by one to verify your local environment:

```bash
# 1. Start PostgreSQL via Docker Compose
docker compose up -d postgres

# 2. Verify database container is healthy
docker compose ps

# 3. Test compilation & test execution via Maven wrapper
# Windows:
.\mvnw.cmd clean test
# macOS/Linux:
./mvnw clean test

# 4. Start the application
# Windows:
.\mvnw.cmd spring-boot:run
# macOS/Linux:
./mvnw spring-boot:run

# 5. In a second terminal, verify the health endpoint:
curl http://localhost:5224/health
```

Expected output from step 5:
```json
{"status":"Healthy","timestamp":"..."}
```

---

## Troubleshooting

- **Port 5432 already in use?**
  You have another PostgreSQL instance running. Stop it (`docker stop <container>`) or change the port mapping in `docker-compose.yml`.
- **`JAVA_HOME` not recognized?**
  Ensure your `JAVA_HOME` environment variable points to your JDK 17 installation directory and `%JAVA_HOME%\bin` is added to your `PATH`.
- **Execution policy error with `mvnw.cmd` on PowerShell?**
  Run: `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` or invoke via `cmd.exe /c mvnw.cmd spring-boot:run`.
