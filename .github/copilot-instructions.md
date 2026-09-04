# Tadka - Copilot Custom Instructions

## Project Context
Tadka is a food delivery platform built as a teaching tool for the Desi Architect cohort. It starts as a Java 17 + Spring Boot 3 monolith and evolves into a modular monolith, then microservices over 16 sessions.

The system handles: restaurant listings, menu management, order placement, delivery tracking, user accounts, and payments.

## Current Architecture (Day 1)
- **Framework:** Spring Boot 3.3.3 (Web, JPA)
- **Language:** Java 17
- **Database:** PostgreSQL 16 via Spring Data JPA (Hibernate ORM)
- **Build Tool:** Maven (using Maven Wrapper `./mvnw` or `.\mvnw.cmd`)
- **Port:** HTTP `5224`

## Coding Standards

### General Java & Spring Boot
- Use modern Java 17 features (`record` for immutable DTOs/value objects, switch expressions, text blocks).
- Standard Spring Boot layered architecture: Controllers -> Services -> Repositories -> Entities.
- REST Controllers annotated with `@RestController` and `@RequestMapping`.
- Use constructor injection (`final` fields) rather than field injection (`@Autowired` on fields).
- Keep Day 1 minimal: bounded contexts and domain folders are introduced from Day 2 onward.

### Naming Conventions
- `PascalCase` for classes, interfaces, records, and enums.
- `camelCase` for method names, variables, and fields.
- Packages in lowercase reverse-domain: `com.tadka.api.*`.
- Suffix conventions: `*Controller`, `*Service`, `*Repository`, `*Dto`, `*Entity`.

### Error Handling & API Responses
- Return RFC 7807 Problem Details (`org.springframework.http.ProblemDetail`) or structured error DTOs for client/server errors.
- Use `@RestControllerAdvice` for global exception handling.
- Wrap HTTP responses with `ResponseEntity<T>`.

### Testing
- JUnit 5 (`org.junit.jupiter.api.*`) and Spring Boot Test (`@SpringBootTest`, `@WebMvcTest`).
- Test naming: `methodName_condition_expectedResult`.

### What NOT to Generate on Day 1
- Do not introduce Kafka, Redis, gRPC, Gateway, or distributed tracing yet.
- Do not create premature empty subpackages or extra service projects.
