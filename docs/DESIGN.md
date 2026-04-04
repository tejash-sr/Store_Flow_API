# StoreFlow API — System Design Document

> **Version:** 1.0  
> **Repository:** https://github.com/tejash-sr/StoreFlowAPI  
> **Organization:** Grootan Technologies – Internal Training Program  
> **Last Updated:** 2026-03-31

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [System Context Diagram](#2-system-context-diagram)
3. [Component Architecture](#3-component-architecture)
4. [Data Architecture](#4-data-architecture)
5. [API Design Principles](#5-api-design-principles)
6. [Security Architecture](#6-security-architecture)
7. [Real-time Architecture (WebSocket)](#7-real-time-architecture-websocket)
8. [File Storage Architecture](#8-file-storage-architecture)
9. [Email Architecture](#9-email-architecture)
10. [Observability Architecture](#10-observability-architecture)
11. [Deployment Architecture](#11-deployment-architecture)
12. [Error Handling Design](#12-error-handling-design)
13. [Scalability Considerations](#13-scalability-considerations)

---

## 1. Architecture Overview

StoreFlow API follows a **classic layered (n-tier) architecture** with stateless REST design and WebSocket support for real-time features:

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                         │
│   (Postman / Frontend App / WebSocket Client)           │
└──────────────────────┬───────────────────┬─────────────┘
                       │ HTTP/REST         │ WS/STOMP
┌──────────────────────▼───────────────────▼─────────────┐
│                   API GATEWAY / NGINX                   │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              SPRING BOOT APPLICATION                    │
│                                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │         MIDDLEWARE PIPELINE (in order)           │   │
│  │  1. RequestLoggingFilter  @Order(1)              │   │
│  │     └─ Assigns UUID trace ID via MDC             │   │
│  │     └─ Adds X-Trace-Id response header           │   │
│  │  2. RateLimitingFilter    @Order(2)              │   │
│  │     └─ Bucket4j: 5 req/15 min/IP on /api/auth/** │   │
│  │     └─ Returns 429 if exceeded                   │   │
│  │  3. JwtAuthenticationFilter (before UsernamePass)│   │
│  │     └─ Extracts Bearer token                     │   │
│  │     └─ Validates JWT signature + expiry          │   │
│  │     └─ Populates Spring SecurityContext          │   │
│  │  4. Spring SecurityFilterChain                   │   │
│  │     └─ Role/authority checks (@PreAuthorize)     │   │
│  │     └─ Returns 401 / 403 as appropriate          │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌───────────────┐ ┌──────────────┐ ┌───────────────┐  │
│  │  CONTROLLERS  │ │  WEBSOCKET   │ │   ACTUATOR    │  │
│  └──────┬────────┘ └──────┬───────┘ └───────────────┘  │
│         │                 │                             │
│  ┌──────▼─────────────────▼──────────────────────────┐  │
│  │                  SERVICE LAYER                    │  │
│  └──────┬────────────────────────────────────────────┘  │
│         │                                               │
│  ┌──────▼────────────────────────────────────────────┐  │
│  │               REPOSITORY LAYER                   │  │
│  └──────┬────────────────────────────────────────────┘  │
└─────────┼───────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────────────────┐
│                    DATA LAYER                           │
│  PostgreSQL 15 (Flyway)  │  File System (uploads/)      │
└─────────────────────────────────────────────────────────┘
```

---

## 2. System Context Diagram

```
                         ┌────────────────────────────────────────────────┐
                         │            StoreFlow API System                │
                         │                                                │
  ┌──────────┐   REST    │  ┌──────────────────────────────────────┐      │
  │  Admin   │──────────►│  │          Spring Boot REST API        │      │
  │  Client  │◄──────────│  │          (Port 8080)                 │      │
  └──────────┘           │  └──────────┬───────────────────────────┘      │
                         │            │                                   │
  ┌──────────┐  REST+WS  │            ▼                                   │
  │   User   │──────────►│  ┌──────────────────────────────────────┐      │   ┌─────────────┐
  │  Client  │◄──────────│  │         PostgreSQL 15 Database       │      │   │ SMTP Server │
  └──────────┘           │  │         (Flyway managed schema)      │◄─────┼──►│ (JavaMail)  │
                         │  └──────────────────────────────────────┘      │   └─────────────┘
  ┌──────────┐           │            │                                   │
  │  Postman │  REST     │            ▼                                   │
  │ / Swagger│──────────►│  ┌──────────────────────────────────────┐      │
  └──────────┘           │  │          File Storage                │      │
                         │  │  (local disk / configurable path)    │      │
                         │  └──────────────────────────────────────┘      │
                         └────────────────────────────────────────────────┘
```

---

## 3. Component Architecture

### 3.1 Spring Boot Layer Responsibilities

```
┌──────────────────────────────────────────────────────────────────────┐
│  @RestController Layer (Controllers)                                 │
│  - Parse HTTP requests                                               │
│  - Validate request format (@Valid)                                  │
│  - Delegate to @Service                                              │
│  - Return ResponseEntity with correct HTTP status                    │
│  - NO business logic                                                 │
│  - NO direct repository access                                       │
├──────────────────────────────────────────────────────────────────────┤
│  @Service Layer (Business Logic)                                     │
│  - All domain business rules live here                               │
│  - Transactional boundaries (@Transactional)                         │
│  - Calls repositories; maps entities to DTOs                        │
│  - Primary test target (unit tests with Mockito)                    │
│  - Throws domain exceptions (ResourceNotFoundException, etc.)        │
├──────────────────────────────────────────────────────────────────────┤
│  @Repository Layer (Data Access)                                     │
│  - Spring Data JPA interfaces                                        │
│  - Custom JPQL queries (@Query)                                      │
│  - Specifications for dynamic queries                                │
│  - Tested with @DataJpaTest + Testcontainers                        │
├──────────────────────────────────────────────────────────────────────┤
│  @Entity Layer (Domain Model)                                        │
│  - JPA-annotated POJOs                                               │
│  - Relationships (@OneToMany, @ManyToOne, @ManyToMany)              │
│  - Embeddables (@Embeddable: ShippingAddress)                        │
│  - Audit fields (createdAt, updatedAt via @EntityListeners)         │
└──────────────────────────────────────────────────────────────────────┘
```

### 3.2 Package Dependency Rules

```
config   ──► (no dependencies on other packages)
entities ──► enums
dto      ──► entities (only for type-safe mapping in .from() factory methods)
repositories ──► entities
services ──► repositories, dto, entities, exceptions, utils
controllers ──► services, dto
middleware ──► utils (JwtUtil), services (UserDetailsService)
exceptions ──► (no dependencies on other packages)
utils    ──► (no dependencies on other packages)
```

---

## 4. Data Architecture

### 4.1 Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────┐
│ USERS                          CATEGORIES                   │
│ ─────                          ──────────                   │
│ id (UUID PK)                   id (UUID PK)                 │
│ email (UNIQUE)                 name (UNIQUE)                │
│ password (BCrypt)              description                  │
│ full_name                      parent_id (FK → categories)  │
│ role (USER/ADMIN)              status                       │
│ avatar_path              1     ─────────────────────────    │
│ reset_token           ┌──── M │                       │    │
│ reset_token_expires_at│        ▼                       │    │
│ enabled               │   PRODUCTS                    │    │
│ created_at            │   ────────                    │    │
│ updated_at            │   id (UUID PK)                │    │
│                       │   name                        │    │
│        │1             │   description                 │    │
│        │              │   sku (UNIQUE)                │    │
│        │ M            │   price                   M   │    │
│        ▼              │   stock_quantity    ──────────┘    │
│   ORDERS              │   category_id (FK)                 │
│   ──────              │   image_url                        │
│   id (UUID PK)        │   status (ACTIVE/INACTIVE/         │
│   reference_number    │          DISCONTINUED)             │
│   customer_id (FK) ───┘   deleted_at                      │
│   status (PENDING/...     created_at                       │
│          DELIVERED)       updated_at                       │
│   shipping_street                                          │
│   shipping_city            ▲                               │
│   shipping_country         │M                              │
│   shipping_postal_code     │                               │
│   total_amount         ORDER_ITEMS                         │
│   created_at          ───────────                          │
│   updated_at          id (UUID PK)                         │
│       │1              order_id (FK) ─────────────────────► │
│       │               product_id (FK)                      │
│       │M              quantity                             │
│       └──────────►    unit_price                           │
│                       subtotal                             │
│                       created_at                           │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 Database Design Decisions

| Decision | Rationale |
|----------|-----------|
| UUID primary keys | Distributed-friendly, no auto-increment collisions, non-guessable |
| Soft-delete for products | Preserve order history referential integrity |
| Price snapshot in OrderItem | Order total is immutable after placement (economic correctness) |
| Embeddable ShippingAddress | Value object — belongs to Order, not its own lifecycle |
| Flyway migrations | Reproducible schema, version-controlled, audit trail |
| Self-referencing Category | Supports nested subcategory tree without extra join table |

### 4.3 Indexing Strategy

| Table | Index | Purpose |
|-------|-------|---------|
| users | `idx_users_email` | Fast login lookup |
| products | `idx_products_sku` | Fast SKU uniqueness check |
| products | `idx_products_category` | Category filtering |
| products | `idx_products_status` | Active product queries |
| orders | `idx_orders_customer` | User's order list |
| orders | `idx_orders_status` | Status-filtered admin queries |
| order_items | `idx_order_items_order` | Order detail loading |

---

## 5. API Design Principles

### 5.1 RESTful Conventions

| Principle | Applied As |
|-----------|-----------|
| Resource nouns in URLs | `/api/products`, `/api/orders` (not `/getProduct`) |
| HTTP verbs for actions | GET=read, POST=create, PUT=full update, PATCH=partial, DELETE=remove |
| Correct status codes | 200=OK, 201=Created, 204=No Content, 400=Bad Request, 401=Unauthorized, 403=Forbidden, 404=Not Found, 409=Conflict, 422=Unprocessable Entity |
| Consistent error envelope | All errors share `{ timestamp, status, error, message, path, errors? }` |
| Plural resource names | `/api/products`, `/api/orders` |
| Sub-resource paths | `/api/products/{id}/image`, `/api/orders/{id}/report` |
| Collection + singleton | `GET /products` (list) vs `GET /products/{id}` (one) |

### 5.2 Pagination Design

**Offset-based** (standard): Used for admin dashboards, data exports.
```
GET /api/products?page=2&size=20&sort=price,asc
```
Response:
```json
{
  "content": [...],
  "page": 2, "size": 20,
  "totalElements": 243,
  "totalPages": 13,
  "first": false, "last": false,
  "hasNext": true
}
```

**Cursor-based** (efficient): Used for infinite scroll / real-time feeds.
```
GET /api/products?cursor=eyJpZCI6IjEyMyJ9&size=20
```
Response:
```json
{
  "content": [...],
  "nextCursor": "eyJpZCI6IjE0MyJ9",
  "hasMore": true,
  "size": 20
}
```

### 5.3 HTTP Status Code Reference

| Code | When to Use |
|------|------------|
| 200 OK | Successful GET, PUT, PATCH |
| 201 Created | Successful POST (new resource created) |
| 204 No Content | Successful DELETE |
| 400 Bad Request | Validation failure, malformed request |
| 401 Unauthorized | Missing or invalid JWT |
| 403 Forbidden | Authenticated but insufficient role |
| 404 Not Found | Resource does not exist |
| 409 Conflict | Duplicate SKU/email, insufficient stock |
| 422 Unprocessable Entity | Invalid state transition |
| 500 Internal Server Error | Unexpected server-side failure |

---

## 6. Security Architecture

### 6.1 Authentication Flow

```
 Client                          StoreFlow API                    PostgreSQL
   │                                    │                              │
   │── POST /api/auth/signup ──────────►│                              │
   │   { email, password, fullName }    │── hash(password, BCrypt)────►│
   │                                    │── INSERT users ─────────────►│
   │                                    │◄─ user saved ────────────────│
   │◄── { accessToken, refreshToken } ──│                              │
   │                                    │                              │
   │── POST /api/auth/login ───────────►│                              │
   │   { email, password }              │── SELECT user by email ─────►│
   │                                    │◄─ user row ──────────────────│
   │                                    │── BCrypt.matches() ──────────│
   │◄── { accessToken, refreshToken } ──│                              │
   │                                    │                              │
   │── GET /api/protected ─────────────►│                              │
   │   Authorization: Bearer <token>    │── JwtAuthFilter validates ───│
   │                                    │── SecurityContext set ────────│
   │◄── 200 OK { data } ────────────────│                              │
```

### 6.2 JWT Structure

```
Header:  { "alg": "HS256", "typ": "JWT" }
Payload: {
  "sub": "user@email.com",
  "userId": "uuid",
  "roles": ["ROLE_USER"],
  "iat": 1711900800,
  "exp": 1711901700   // 15 minutes
}
Signature: HMAC-SHA256(base64url(header) + "." + base64url(payload), SECRET)
```

**Access Token:** 15 minutes expiry  
**Refresh Token:** 7 days expiry, stored in DB for revocation support

### 6.3 Authorization Matrix

| Resource | GET | POST | PUT | PATCH | DELETE |
|----------|-----|------|-----|-------|--------|
| `/api/products` | Public | ADMIN | ADMIN | ADMIN | ADMIN |
| `/api/orders` | USER (own) / ADMIN (all) | USER | — | ADMIN | — |
| `/api/auth/**` | USER | Public | — | — | — |
| `/api/admin/**` | ADMIN | ADMIN | ADMIN | — | — |
| `/actuator/**` | ADMIN | — | — | — | — |

### 6.4 Password Reset Flow

```
 User                StoreFlow API              Email Service (Mock in tests)
  │                       │                              │
  │── POST /forgot-pass ──►│                              │
  │   { email }            │── generate time-limited token│
  │                        │── store resetToken in DB ────│
  │                        │── build reset URL ───────────►│
  │                        │                              │── send HTML email
  │◄── 200 OK ─────────────│                              │
  │                        │                              │
  │── POST /reset-pass ────►│                              │
  │   { token, newPass }   │── findByResetToken ──────────│
  │                        │── isResetTokenValid() ────────│
  │                        │── BCrypt.hash(newPass) ───────│
  │                        │── UPDATE user.password ───────│
  │                        │── clear resetToken ───────────│
  │◄── 200 OK ─────────────│                              │
```

### 6.5 Rate Limiting Design

```
Rate Limiter: Bucket4j (token bucket algorithm)
Scope: Per IP address, per endpoint group
Config for /api/auth/**:
  - Capacity: 5 tokens
  - Refill: 5 tokens per 15 minutes
  - Algorithm: Token Bucket
  - Response when exceeded: HTTP 429 Too Many Requests
```

---

## 7. Real-time Architecture (WebSocket)

### 7.1 STOMP Message Flow

```
 Client (subscriber)          StoreFlow API           PATCH /orders/{id}/status
        │                          │                         │
        │── WS handshake + JWT ───►│                         │
        │                          │── validate JWT          │
        │◄── connection accepted ──│                         │
        │                          │                         │
        │── SUBSCRIBE ────────────►│                         │
        │  /topic/orders/123/status│                         │
        │                          │                         │
        │                          │◄── Admin updates status─│
        │                          │── OrderService.update() │
        │                          │── NotificationService   │
        │                          │   .publishStatusChange()│
        │◄── MESSAGE ─────────────│                         │
        │  { newStatus, timestamp }│                         │
```

### 7.2 STOMP Destination Mapping

| Destination | Type | Who Subscribes | Trigger |
|-------------|------|---------------|---------|
| `/topic/orders/{orderId}/status` | Topic | Anyone interested in order | Order status updated |
| `/user/{userId}/queue/notifications` | Queue | Specific user | Personal event |
| `/app/...` | App endpoint | Clients sending messages | Client-initiated |

### 7.3 WebSocket Security

```
Handshake Interceptor:
  1. Extract JWT from Authorization header or ?token= query param
  2. Validate JWT signature + expiry
  3. If invalid: close connection with 401 UNAUTHORIZED
  4. If valid: set userId in WebSocket session attributes
```

---

## 8. File Storage Architecture

### 8.1 File Storage Strategy

```
File Storage (Local in Dev, Object Storage in Prod)
└── uploads/
    ├── products/
    │   └── {productId}/
    │       └── image.{ext}
    └── avatars/
        └── {userId}/
            └── avatar.{ext}
```

Configuration:

```yaml
storage:
  base-path: ${STORAGE_PATH:./uploads}
  max-file-size: 5MB
  allowed-image-types:
    - image/jpeg
    - image/png
    - image/webp
```

### 8.2 File Upload Flow

```
 Client                    ProductController          FileStorageService
   │                             │                          │
   │── POST /products/{id}/image │                          │
   │   Content-Type: multipart   │                          │
   │   [file bytes]              │                          │
   │                             │── validate size + MIME ──│
   │                             │── if invalid → 400 ──────│
   │                             │── storeFile(bytes) ──────►│
   │                             │                          │── write to disk
   │                             │◄── filePath ─────────────│
   │                             │── product.imageUrl = path│
   │                             │── productRepo.save() ─────│
   │◄── 200 { imageUrl } ────────│                          │
```

### 8.3 PDF Generation Flow

```
GET /api/orders/{id}/report
         │
         ▼
   OrderController
         │── orderService.findById(id) (with auth check)
         │── pdfService.generateOrderReport(order)
         │       │── PDDocument doc = new PDDocument()
         │       │── build pages: header, items table, totals
         │       │── return byte[]
         │── ResponseEntity
         │       Content-Type: application/pdf
         │       Content-Disposition: attachment; filename="order-{ref}.pdf"
         │── stream bytes to client
```

---

## 9. Email Architecture

### 9.1 Email Service Design

```
┌──────────────────┐       ┌──────────────────┐      ┌──────────────────┐
│   Triggering     │       │   EmailService   │      │  JavaMailSender  │
│   Services       │       │                  │      │  (DEV: Greenmail)│
│                  │       │  sendWelcome()   │      │  (PROD: SMTP)    │
│  AuthService    ─┼──────►│  sendReset()     ├─────►│                  │
│  OrderService   ─┼──────►│  sendConfirm()   │      │  HTML Templates  │
│  ScheduledJob   ─┼──────►│  sendLowStock()  │      │  (Thymeleaf or   │
│                  │       │  sendDigest()    │      │   plain String)  │
└──────────────────┘       └──────────────────┘      └──────────────────┘
```

### 9.2 Email Test Strategy

In `application-test.yml`:
```yaml
spring:
  mail:
    host: localhost
    port: 3025   # Greenmail embedded SMTP

# OR: use a mock JavaMailSender bean in test profile
```

Tests verify:
1. `mailSender.send()` was called with correct recipient
2. Subject contains expected text
3. Body contains order reference / reset link

---

## 10. Observability Architecture

### 10.1 Logging Strategy

```
Every HTTP Request:
  [2026-03-31T12:00:00Z] [a3b7-f2c9-...] INFO  RequestLoggingFilter - GET /api/products 200 45ms
  [2026-03-31T12:00:00Z] [a3b7-f2c9-...] DEBUG ProductService - Fetching products with filter: category=Electronics

MDC Context:
  traceId: UUID per request (set in RequestLoggingFilter)
  userId:  authenticated user ID (set after JWT validation)

Logback Pattern:
  %d{ISO8601} [%X{traceId}] [%X{userId}] %-5level %logger{36} - %msg%n
```

### 10.2 Metrics Architecture

```
Micrometer Metrics (exposed via /actuator/prometheus):

Counter: orders_placed_total
  ├── incremented on each successful order placement
  └── tags: { status=SUCCESS }

Counter: orders_revenue_total
  ├── incremented by order.totalAmount on each placement
  └── tags: { currency=USD }

Gauge: orders_value_average
  └── rolling average of order total amounts

Timer: http_server_requests (auto by Actuator)
  └── per endpoint response times

Gauge: product_low_stock_count
  └── count of products below threshold
```

### 10.3 Actuator Endpoint Security

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when-authorized  # prod: never; dev: always

# Secure /actuator/** to ADMIN role in SecurityConfig
```

---

## 11. Deployment Architecture

### 11.1 Local Development

```
┌────────────────────────────────────────────────┐
│  Developer Machine                             │
│                                                │
│  ┌────────────────┐    ┌───────────────────┐  │
│  │  Spring Boot   │    │  Docker Container │  │
│  │  Application   │◄──►│  PostgreSQL 15    │  │
│  │  (Port 8080)   │    │  (Port 5432)      │  │
│  └────────────────┘    └───────────────────┘  │
│  spring.profiles.active=dev                   │
└────────────────────────────────────────────────┘
```

### 11.2 Containerized (docker-compose)

```
┌──────────────────────────────────────────────────────────┐
│  Docker Compose Network                                  │
│                                                          │
│  ┌──────────────────┐           ┌────────────────────┐  │
│  │  app container   │ internal  │  postgres container│  │
│  │  Port 8080:8080  │──────────►│  Port 5432         │  │
│  │  Profile: prod   │           │  Volume: postgres  │  │
│  └──────────────────┘           └────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

### 11.3 Dockerfile

```dockerfile
# Use ARG to pin the artifact name explicitly — avoids multi-JAR Docker COPY failure
ARG JAR_FILE=target/storeflow-api-0.0.1-SNAPSHOT.jar
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

### 11.4 CI/CD Pipeline Flow

```
Developer Push
     │
     ▼
GitHub Actions Trigger
     │
     ├── Lint Check (Checkstyle / SpotBugs)
     ├── Unit Tests (./mvnw test -P unit)
     ├── Integration Tests (./mvnw verify -P integration)
     │     └── Testcontainers: spin up PostgreSQL
     ├── JaCoCo Coverage Check (>= 80%)
     ├── SonarQube Analysis
     │
     ▼ (on main branch only)
Build Docker Image
     │
     ▼
Push to Container Registry
     │
     ▼
Deploy to Staging
     │ (Manual approval)
     ▼
Deploy to Production
```

---

## 12. Error Handling Design

### 12.1 Exception Hierarchy

```
java.lang.RuntimeException
└── AppException (base)
    ├── ResourceNotFoundException      → 404 Not Found
    ├── InsufficientStockException     → 409 Conflict
    ├── InvalidStatusTransitionException → 422 Unprocessable Entity
    ├── AuthenticationFailedException  → 401 Unauthorized
    └── AccessDeniedException          → 403 Forbidden

javax.validation.ConstraintViolationException → 400 Bad Request
org.springframework.web.bind.MethodArgumentNotValidException → 400
org.springframework.dao.DataIntegrityViolationException → 409
io.jsonwebtoken.JwtException → 401
java.lang.Exception (catch-all) → 500
```

### 12.2 Error Response Shape

```json
{
  "timestamp": "2026-03-31T12:34:56.789Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for 2 fields",
  "path": "/api/products",
  "errors": {
    "name": "must not be blank",
    "price": "must be greater than 0"
  }
}
```

### 12.3 GlobalExceptionHandler Methods

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // → 400 with field-level errors map

    @ExceptionHandler(AppException.class)
    // → uses exception.getStatusCode()

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    // → 409 with conflicting field identified

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    // → 401

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    // → 500; suppress stack trace in prod
}
```

---

## 13. Scalability Considerations

### 13.1 Stateless Design
- No server-side session state; all state in JWT
- WebSocket connections managed per-instance (horizontal scaling requires a message broker like Redis Pub/Sub or RabbitMQ for STOMP in production)

### 13.2 Database Optimization
- Connection pooling via HikariCP (Spring Boot default)
- Strategic indexes (see Section 4.3)
- Soft-delete avoids cascading deletes that lock tables
- Cursor-based pagination avoids `OFFSET` performance degradation for large datasets

### 13.3 Future Scalability Path
- Replace local file storage with object storage (AWS S3 / GCS)
- Add Redis caching layer for product catalog
- Move email sending to message queue (RabbitMQ / SQS)
- Add read replicas for heavy read workloads
- Implement CQRS pattern for order processing at scale

### 13.4 Performance Targets

| Metric | Target |
|--------|--------|
| GET /api/products | < 100ms (cached) |
| POST /api/orders | < 500ms (transactional) |
| POST /api/auth/login | < 300ms |
| PDF generation | < 2s |
| WebSocket notification latency | < 100ms |
