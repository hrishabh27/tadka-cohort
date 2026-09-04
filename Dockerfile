# Multi-stage Dockerfile for Tadka Spring Boot API

# Stage 1: Build
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

# Copy maven wrapper and pom.xml first for caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source code and build package
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Run as non-root user for security
RUN groupadd -r tadka && useradd -r -g tadka tadka
USER tadka

COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 5224

ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]
