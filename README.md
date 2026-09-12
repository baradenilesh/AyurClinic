# AyurClinic — Sprint 0 + Sprint 1

Executable foundation for the AyurClinic modular monolith.

## Stack
- Java 21
- Spring Boot 4.1.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- Docker Compose
- Testcontainers-ready test structure

## Run locally

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

Or:

```bash
docker compose up --build
```

API base: `http://localhost:8080/api/v1`

Health: `http://localhost:8080/actuator/health`

## Initial endpoint

`POST /api/v1/tenants`

Example:

```json
{
  "name": "AyurClinic Pilot"
}
```

## Project modules

The code is a modular monolith. Sprint 1 implements:
- common
- tenant
- clinic
- user foundation
- doctor foundation
- patient foundation
- Flyway database foundation

Authentication/JWT is intentionally Sprint 2.
