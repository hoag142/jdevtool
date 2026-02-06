# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DevTools for Backend - A self-hosted, all-in-one web app providing developer utility tools (JWT decoder, UUID generator, Base64, JSON-to-Java converter, Cron builder, Regex tester, Timestamp converter, Hash generator, SQL formatter). Built with Spring Boot + Thymeleaf + HTMX for server-side rendering with dynamic updates.

## Build & Run Commands

```bash
# Run locally (requires Redis on localhost:6379)
./mvnw spring-boot:run

# Run with Docker (recommended - includes Redis)
docker-compose up --build

# Compile only
./mvnw compile

# Run tests
./mvnw test

# Run single test class
./mvnw test -Dtest=JwtServiceTest

# Package as JAR
./mvnw package -DskipTests
```

## Architecture

### Request Flow
```
Browser → Controller (Thymeleaf page) → Service (business logic) → Response (HTML fragment via HTMX)
```

### URL Pattern for Tools
- `GET /tools/{toolName}` - Render full tool page
- `POST /tools/{toolName}/{action}` - Execute action, return HTML fragment for HTMX swap

Example: JWT tool
- `GET /tools/jwt` - JWT decoder page
- `POST /tools/jwt/decode` - Decode JWT, return result fragment
- `POST /tools/jwt/verify` - Verify signature, return result fragment

### Key Patterns

**Controller Pattern**: Each tool has its own controller with a shared `TOOLS` list for sidebar navigation. Controllers return Thymeleaf fragments for HTMX requests.

**Service Pattern**: Business logic in services, returning `Map<String, Object>` for flexible template binding.

**Template Pattern**: Tools use Thymeleaf fragments (`th:fragment`) for partial page updates via HTMX. Layout uses `th:replace` for composition.

### Frontend Stack
- **TailwindCSS** via CDN for styling
- **HTMX** for AJAX without JavaScript (`hx-post`, `hx-target`, `hx-swap`)
- **Alpine.js** for client-side interactivity (sidebar toggle, dark mode)

### Redis Data Structures
- `history:{userId}` - List of recent tool usage (TTL: 7 days, max 50 items)
- `snippet:{shortId}` - Hash for shared snippets (TTL: 24 hours)
- `stats:tools` - Sorted set for usage statistics

## Tools to Implement

| Tool | Service | Key Libraries |
|------|---------|---------------|
| JWT Decoder | JwtService | jjwt (0.12.3) |
| UUID Generator | UuidService | java-uuid-generator (4.3.0) |
| Base64 | Base64Service | commons-codec (1.16.0) |
| JSON to Java | JsonToJavaService | jackson-databind |
| Cron Builder | CronService | cron-utils (9.2.1) |
| Regex Tester | RegexService | java.util.regex |
| Timestamp | TimestampService | java.time |
| Hash Generator | HashService | commons-codec, spring-security-crypto |
| SQL Formatter | SqlService | sql-formatter (2.0.4) |

## Configuration

Redis connection configured via environment variable `SPRING_REDIS_HOST` (default: localhost).
