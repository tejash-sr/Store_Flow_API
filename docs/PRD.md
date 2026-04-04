# StoreFlow API — Product Requirements Document (PRD)

> **Version:** 1.0  
> **Repository:** https://github.com/tejash-sr/StoreFlowAPI  
> **Organization:** Grootan Technologies – Internal Training Program  
> **Classification:** Confidential – Internal Use Only  
> **Last Updated:** 2026-03-31  
> **Author:** Tejash | Reviewer: Grootan Training Team

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Product Vision & Goals](#2-product-vision--goals)
3. [Stakeholders](#3-stakeholders)
4. [Scope & Boundaries](#4-scope--boundaries)
5. [Tech Stack](#5-tech-stack)
6. [Middleware Requirements](#6-middleware-requirements)
7. [Functional Requirements — All 8 Phases](#7-functional-requirements--all-8-phases)
8. [Non-Functional Requirements](#8-non-functional-requirements)
9. [Complete API Endpoint Catalogue](#9-complete-api-endpoint-catalogue)
10. [Security Requirements](#10-security-requirements)
11. [Testing Requirements](#11-testing-requirements)
12. [Delivery & Submission Checklist](#12-delivery--submission-checklist)
13. [Evaluation Rubric](#13-evaluation-rubric)
14. [Bonus Challenges](#14-bonus-challenges)

---

## 1. Executive Summary

**StoreFlow API** is a production-grade, fully-tested **Inventory & Order Management REST API** built as a comprehensive internal training exercise at Grootan Technologies. It is structured into **8 progressive phases**, each with formal acceptance criteria and minimum test counts. Test-Driven Development (TDD) is practiced throughout.

### Courses Covered

| Course | Topics |
|--------|--------|
| Spring Framework 6: Beginner to Guru | Spring Boot 3, MVC, JPA, Security, WebSocket, Mail, Actuator |
| Spring Boot Unit Testing with JUnit, Mockito & Testcontainers | TDD, Mockito, MockMvc, @DataJpaTest, Integration Tests |
| PostgreSQL Bootcamp: Complete Beginner to Advanced | Schema design, indexes, Flyway migrations |

---

## 2. Product Vision & Goals

### Vision
> Build a fully tested, production-quality REST API that covers 90%+ of the concepts from all three courses, demonstrating real-world backend engineering skills with Java and Spring Boot.

### Goals

| ID | Goal | Success Metric |
|----|------|---------------|
| G1 | Complete all 8 phases | All acceptance criteria met |
| G2 | High test coverage | JaCoCo >= 80% lines, branches, methods, instructions |
| G3 | Minimum test count | >= 80 total tests (unit + integration) |
| G4 | Clean layered architecture | Zero business logic in controllers |
| G5 | TDD discipline | Tests written before or alongside implementation |
| G6 | Production-ready middleware | JWT filter, logging filter, rate-limit filter all operational |
| G7 | Real-time system | WebSocket STOMP notifications working |
| G8 | Production hardening | Multi-profile config, graceful shutdown, Actuator |

---

## 3. Stakeholders

| Role | Person / Group | Responsibility |
|------|---------------|----------------|
| Developer | Tejash | Implement all 8 phases and produce all deliverables |
| Reviewer | Grootan Training Team | Evaluate, grade, and give feedback |
| Internal Reviewer | Senior Engineer | Code review for quality and architecture compliance |

---

## 4. Scope & Boundaries

### In Scope

| Area | Details |
|------|---------|
| Data Management | Full CRUD — Products, Categories, Orders, Users |
| Authentication | JWT access + refresh tokens, password reset via email |
| Authorization | Role-based — USER and ADMIN |
| **Middleware** | **JWT filter, request logging (MDC), rate limiting — all mandatory** |
| File Operations | Product image upload/download, user avatar, PDF reports, CSV exports |
| Real-time | WebSocket STOMP notifications for order status changes |
| Email | 5 email types, mocked in tests |
| Observability | Spring Actuator, Micrometer metrics, structured logging |
| Testing | Full test pyramid — unit, repository, integration — min 80 tests |
| Database | PostgreSQL 15 with Flyway version-controlled migrations |
| CI/CD | GitHub Actions pipeline; coverage gate enforced |

### Out of Scope
- Frontend / UI (pure API)
- Payment gateway integration
- Shipping carrier integration
- Multi-tenant architecture

---

## 5. Tech Stack

| Category | Technology | Version/Notes |
|----------|-----------|--------------|
| Runtime | Java | 21 (minimum 17) |
| Framework | Spring Boot | 3.x |
| Web | Spring MVC | REST controllers |
| Persistence | Spring Data JPA + Hibernate | ORM |
| Database | PostgreSQL | 15 — Flyway managed |
| Auth | Spring Security + JJWT + BCrypt | Stateless JWT |
| **Middleware** | **OncePerRequestFilter (x3)** | **JWT, Logging, Rate Limiting** |
| Rate Limiting | Bucket4j | Token bucket, per-IP |
| Testing | JUnit 5 + Mockito + Testcontainers + MockMvc | No JUnit 4 vintage engine |
| Validation | Jakarta Bean Validation + Custom Validators | @NotBlank, @ExistsInDatabase, @ValidSku |
| File Handling | Spring Multipart + Apache PDFBox | Upload, PDF, Thumbnailator for avatar |
| Email | Spring Mail (JavaMailSender) | Greenmail in tests |
| Real-time | Spring WebSocket + STOMP + SockJS | Order status notifications |
| Build | Maven | With `./mvnw` wrapper |
| Coverage | JaCoCo | 80% gate at `mvn verify` |
| Metrics | Spring Actuator + Micrometer | Custom counters/gauges |

---

## 6. Middleware Requirements

> **Middleware is mandatory and critical.** It is not optional plumbing — it is what makes the API secure, observable, and resilient.

All middleware filters extend `OncePerRequestFilter` (guaranteed single execution per request, even with async dispatch).

### MW-1: RequestLoggingFilter

**Priority / Order:** 1 (first filter executed)  
**Package:** `com.grootan.storeflow.middleware`

**Responsibilities:**
- Generate a unique `UUID` trace ID for every incoming request
- Store it in MDC (`MDC.put("traceId", traceId)`) so it appears in every log line
- Add it to the response as `X-Trace-Id` header for client-side correlation
- Log: HTTP method, URI, response status, duration in ms
- Clear MDC after the request completes (`MDC.clear()`)

**Why it matters:**  
Without this, debugging production issues is nearly impossible — you cannot correlate log lines from the same request. Every log line must carry the trace ID.

**Required Tests:**
- Unit: filter sets `X-Trace-Id` header on every response
- Unit: MDC is cleared after request completes (no trace ID leaks between requests)
- Integration: every response contains `X-Trace-Id` header

---

### MW-2: RateLimitingFilter

**Priority / Order:** 2  
**Package:** `com.grootan.storeflow.middleware`  
**Active Routes:** `/api/auth/**` only

**Responsibilities:**
- Use Bucket4j token bucket algorithm, keyed by remote IP address
- Allow maximum 5 requests per 15-minute window per IP
- On limit exceeded: return `429 Too Many Requests` with body `{ "error": "Too many requests. Try again later." }`
- Bypass all non-auth routes (proceed to next filter without checking)

**Why it matters:**  
Without rate limiting, auth endpoints are vulnerable to brute-force credential stuffing attacks. This is a mandatory security requirement for any production API.

**Required Tests:**
- Unit: returns 429 after exceeding 5 requests for the same IP
- Unit: non-auth routes bypass rate limiting regardless of request count
- Integration: 6th consecutive auth request from the same IP returns 429

---

### MW-3: JwtAuthenticationFilter

**Priority / Order:** 3 (registered via `addFilterBefore(UsernamePasswordAuthenticationFilter.class)`)  
**Package:** `com.grootan.storeflow.middleware`

**Responsibilities:**
- Read the `Authorization` header
- If it starts with `Bearer `, extract the token string
- Call `JwtUtil.extractUsername(token)` — this throws `JwtException` for invalid/expired tokens
- If valid: load `UserDetails`, set `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`
- If no header, or not a Bearer token: pass through without setting authentication (Spring Security will reject unauthenticated requests on protected routes)
- If JWT is invalid/expired: return 401 with structured error body (do NOT propagate to next filter)

**Why it matters:**  
This is the security gate for the entire API. Without it, JWT tokens are meaningless — any request would be treated as unauthenticated. Spring Security's built-in filters do not parse JWTs; this custom filter is what makes JWT auth work.

**Required Tests:**
- Unit: valid JWT → SecurityContext populated correctly
- Unit: missing Authorization header → filter passes through, no auth set
- Unit: expired JWT → 401 returned, filter chain stops
- Unit: malformed JWT string → 401 returned
- Unit: wrong signature JWT → 401 returned
- Integration: valid Bearer token on protected route → 200 OK
- Integration: no token on protected route → 401
- Integration: expired token → 401 with structured error body

---

### Filter Registration Order Summary

```java
// In SecurityConfig.java — register filters in correct order:
http
    .addFilterBefore(requestLoggingFilter, ChannelProcessingFilter.class)   // First
    .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

---

## 7. Functional Requirements — All 8 Phases

---

### Phase 1 — Project Foundation & Spring Boot Setup

**Topics:** Spring Boot Basics, Spring MVC, Application Configuration, JUnit 5, MockMvc  
**Minimum Tests:** 6

#### FR-1.1 Project Setup
- Spring Boot 3 project with dependencies: Web, Data JPA, Security, PostgreSQL Driver, Flyway, Validation, Lombok, Actuator
- Configuration via `application.yml` (never `application.properties`)
- Entry point class (`StoreFlowApplication`) separate from configuration classes
- Packages created: `config`, `controllers`, `services`, `repositories`, `entities`, `dto`, `middleware`, `exceptions`, `validation`, `enums`, `utils`, `metrics`

#### FR-1.2 Test Infrastructure
- JUnit 5; JUnit 4 vintage engine **explicitly disabled** in `pom.xml`
- `AbstractIntegrationTest` base class: `@SpringBootTest` + `@Testcontainers` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`
- Testcontainers PostgreSQL container with `@DynamicPropertySource`
- JaCoCo plugin configured with 80% minimum enforced at `mvn verify`

#### FR-1.3 Middleware Scaffold (Phase 1)
Create placeholder implementations of all 3 filters in the `middleware` package. Even if JWT logic is not complete until Phase 4, the classes must exist and be registered in the filter chain from Phase 1.

#### FR-1.4 Health Endpoint
- `GET /api/health` → `{ "status": "UP", "timestamp": "...", "jvmUptimeMs": 120000 }`
- `GET /api/health` returns HTTP 200

#### FR-1.5 Catch-All 404 & Global Exception Handler
- Any unmatched route → HTTP 404 + `{ timestamp, status, error, message, path }`
- `GlobalExceptionHandler` (`@ControllerAdvice`) handles all uncaught exceptions

#### FR-1.6 CORS & Security Baseline
- CORS configured (all origins in dev, restricted in prod)
- CSRF disabled (stateless API)
- All routes publicly accessible in Phase 1

#### FR-1.7 Acceptance Criteria
- [ ] Spring context loads without errors from a fresh clone
- [ ] Application starts with `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- [ ] All 3 middleware filter classes exist in `middleware` package
- [ ] At least **6 passing tests**

---

### Phase 2 — Data Models & Database Layer

**Topics:** JPA Entities, Hibernate Relationships, PostgreSQL Schema, Flyway, @DataJpaTest, Testcontainers  
**Minimum Tests:** 15

#### FR-2.1 Entities to Implement

| Entity | Key Constraints |
|--------|----------------|
| `User` | email (unique), password (BCrypt, @JsonIgnore), fullName (2-100 chars), role (USER/ADMIN default USER), avatarPath (optional), resetToken + resetTokenExpiresAt (optional), enabled boolean, createdAt/updatedAt via @EntityListeners |
| `Category` | name (unique), description (optional), parent @ManyToOne self-ref (nullable), status |
| `Product` | name (3-150 chars), description (max 3000), sku (unique, uppercase), price (positive), stockQuantity (non-negative), category @ManyToOne, imageUrl (optional), status (ACTIVE/INACTIVE/DISCONTINUED), deletedAt (for soft delete) |
| `Order` | referenceNumber (unique, auto-generated), customer @ManyToOne User, orderItems @OneToMany (cascade ALL), status enum, shippingAddress @Embeddable, totalAmount, timestamps |
| `OrderItem` | order @ManyToOne, product @ManyToOne, quantity (positive), unitPrice (price snapshot), subtotal |
| `ShippingAddress` | @Embeddable — street, city, country, postalCode |

#### FR-2.2 User Transient Helper
`isResetTokenValid()` — returns `true` only if `resetToken != null` AND `resetTokenExpiresAt` is in the future.

#### FR-2.3 Custom Repository Methods
- `ProductRepository.findBySkuIgnoreCase(String sku)`
- `ProductRepository.findByCategory(Category category)`
- `ProductRepository.findAllByStockQuantityLessThan(int threshold)`
- `UserRepository.findByEmail(String email)`
- `UserRepository.findByResetToken(String token)`

#### FR-2.4 Flyway Migrations
```
V1__create_users.sql
V2__create_categories.sql
V3__create_products.sql
V4__create_orders.sql
```

#### FR-2.5 Acceptance Criteria
- [ ] All 5 entities and 1 embeddable implemented with correct JPA annotations
- [ ] Flyway migrations version-controlled; apply cleanly on fresh schema
- [ ] At least **15 @DataJpaTest + Testcontainers tests** passing

---

### Phase 3 — REST API Endpoints & CRUD

**Topics:** Spring MVC Controllers, ResponseEntity, Service Layer Pattern, MockMvc  
**Minimum Tests:** 20

#### FR-3.1 Endpoints

| # | Method | Path | Description |
|---|--------|------|-------------|
| 1 | POST | `/api/products` | Create product |
| 2 | GET | `/api/products` | List products (filters + pagination) |
| 3 | GET | `/api/products/{id}` | Get single product with category |
| 4 | PUT | `/api/products/{id}` | Full update |
| 5 | PATCH | `/api/products/{id}/stock` | Adjust stock (atomic) |
| 6 | DELETE | `/api/products/{id}` | Soft-delete (DISCONTINUED + deletedAt) |
| 7 | POST | `/api/orders` | Place order (validate stock → deduct → calc total) |
| 8 | GET | `/api/orders` | List orders (USER: own; ADMIN: all) |
| 9 | GET | `/api/orders/{id}` | Order detail with all items |
| 10 | PATCH | `/api/orders/{id}/status` | Update status (admin, valid transitions only) |

Query parameters for `GET /api/products`: `page`, `size`, `sort`, `category`, `status`, `minPrice`, `maxPrice`, `name`

#### FR-3.2 Business Rules
- Controllers are **thin** — parse request, delegate to service, return response
- No business logic anywhere except the `@Service` layer
- Stock deduction is **atomic** (validate all items first, then deduct — never partial)
- Soft-delete: sets `status = DISCONTINUED`, records `deletedAt = now()`
- Valid order status transitions: `PENDING→CONFIRMED`, `CONFIRMED→SHIPPED`, `SHIPPED→DELIVERED`, `PENDING→CANCELLED`, `CONFIRMED→CANCELLED`

#### FR-3.3 Acceptance Criteria
- [ ] All 10 endpoints functional and return correct HTTP status codes
- [ ] At least **20 tests** (6+ unit with Mockito, 10+ integration with MockMvc + Testcontainers)
- [ ] Pagination works correctly with `page`/`size` params
- [ ] Stock deduction is atomic (no partial deductions on failure)

---

### Phase 4 — Authentication & Authorization

**Topics:** Spring Security, JWT, SecurityFilterChain, Method-level Security, Password Reset  
**Minimum Tests:** 14

#### FR-4.1 Auth Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/signup` | Register, BCrypt hash, return JWT |
| POST | `/api/auth/login` | Verify credentials, return access + refresh tokens |
| POST | `/api/auth/refresh` | New access token from refresh token |
| POST | `/api/auth/forgot-password` | Generate reset token, send email |
| POST | `/api/auth/reset-password/{token}` | Reset password using valid token |
| GET | `/api/auth/me` | Get current user profile |
| PUT | `/api/auth/me/avatar` | Upload avatar |

#### FR-4.2 Complete Middleware Implementation (Phase 4)
All 3 middleware filters must be **fully implemented** in this phase:
- `JwtAuthenticationFilter` — full JWT extraction, validation, SecurityContext population
- `RateLimitingFilter` — Bucket4j token bucket on `/api/auth/**`
- `RequestLoggingFilter` — already functional from Phase 1

#### FR-4.3 Role Authorization Matrix

| Resource | Public | USER | ADMIN |
|----------|--------|------|-------|
| GET /api/products | ✓ | ✓ | ✓ |
| POST/PUT/DELETE /api/products | ✗ | ✗ | ✓ |
| POST /api/orders | ✗ | ✓ | ✓ |
| GET /api/orders (own) | ✗ | ✓ | ✓ |
| GET /api/orders (all) | ✗ | ✗ | ✓ |
| PATCH /api/orders/{id}/status | ✗ | ✗ | ✓ |
| GET /api/admin/** | ✗ | ✗ | ✓ |
| GET /actuator/metrics | ✗ | ✗ | ✓ |

#### FR-4.4 Acceptance Criteria
- [ ] Complete JWT auth (access + refresh tokens)
- [ ] All 3 middleware filters fully implemented
- [ ] All Phase 3 integration tests updated with `Authorization: Bearer <token>` header
- [ ] Password reset flow works end-to-end (with mocked email)
- [ ] At least **14 new auth + middleware tests**

---

### Phase 5 — Validation & Error Handling

**Topics:** Jakarta Bean Validation, Custom ConstraintValidator, @ControllerAdvice  
**Minimum Tests:** 12

#### FR-5.1 Validation Rules

| Field | Rule |
|-------|------|
| Product `name` | Required, trimmed, 3-150 chars |
| Product `price` | Required, positive decimal |
| Product `stockQuantity` | Non-negative integer |
| Product `sku` | Required, matches `^[A-Z0-9-]+$` |
| Product `categoryId` | Must reference an existing category — `@ExistsInDatabase` |
| Order `quantity` | Positive integer per item |
| Shipping address | All fields required; `postalCode` must match country-specific regex |
| `email` fields | Valid format; normalized to lowercase before persistence |
| All strings | Strip leading/trailing whitespace |

#### FR-5.2 Custom Exceptions

```
AppException (base — has HttpStatus field)
├── ResourceNotFoundException       → 404
├── InsufficientStockException      → 409
├── InvalidStatusTransitionException → 422
├── AuthenticationFailedException   → 401
└── AccessDeniedException           → 403
```

#### FR-5.3 GlobalExceptionHandler Mappings

| Exception | HTTP Status | Notes |
|-----------|-------------|-------|
| `MethodArgumentNotValidException` | 400 | Include field-level `errors` map |
| `ConstraintViolationException` | 400 | Bean validation on path/query params |
| `DataIntegrityViolationException` | 409 | Identify conflicting field |
| `JwtException` | 401 | Token expired or malformed |
| `AppException` subclasses | From exception | Use `exception.getHttpStatus()` |
| `Exception` (catch-all) | 500 | No stack trace in prod profile |

#### FR-5.4 Acceptance Criteria
- [ ] Every POST/PUT endpoint has `@Valid` on request body
- [ ] All custom exception types used in service layer
- [ ] `GlobalExceptionHandler` covers every mapped exception type
- [ ] At least **12 new tests**
- [ ] Zero unhandled exceptions reaching the client as unexpected 500s

---

### Phase 6 — File Upload, Download & PDF Generation

**Topics:** Spring Multipart, InputStreamResource, Apache PDFBox, Thumbnailator, CSV  
**Minimum Tests:** 8

#### FR-6.1 Features

| # | Method | Path | Description |
|---|--------|------|-------------|
| 11 | POST | `/api/products/{id}/image` | Upload product image (max 5MB, jpeg/png/webp) |
| 12 | GET | `/api/products/{id}/image` | Stream image back with correct Content-Type |
| 13 | PUT | `/api/auth/me/avatar` | Upload + resize avatar (max 5MB, Thumbnailator) |
| 14 | GET | `/api/orders/{id}/report` | Stream PDF (reference, items, total) |
| 15 | GET | `/api/orders/export` | Download CSV with `?from=&to=` date filter |

File storage: path configured via `storage.base-path` property — not hardcoded.

#### FR-6.2 Acceptance Criteria
- [ ] Upload validates size (< 5MB) and MIME type
- [ ] PDF streams as `application/pdf` with correct filename
- [ ] CSV has header row + one row per OrderItem
- [ ] At least **8 new tests**

---

### Phase 7 — Advanced Queries, Pagination & Real-time

**Topics:** JPQL, JPA Specifications, Pageable, WebSocket STOMP, Cursor Pagination  
**Minimum Tests:** 10

#### FR-7.1 Dual-Mode Pagination

**Offset-based:** `page`, `size`, `sort` params  
Response: `content, page, size, totalElements, totalPages, first, last, hasNext`

**Cursor-based:** `cursor`, `size` params  
Response: `content, nextCursor, hasMore, size`

Defaults: page size = 20, cap = 100. Handle: page beyond last, size <= 0, empty result.

Multi-field `sort`: `createdAt`, `price`, `name`, `stockQuantity`

#### FR-7.2 Product Search
Flexible single-query endpoint using JPA Specification or JPQL:
- `name`: partial, case-insensitive
- `category`: by ID
- `minPrice` / `maxPrice`: range
- `status`: exact match

Admin: `GET /api/admin/products/low-stock?threshold=10` via custom `@Query` JPQL.

#### FR-7.3 WebSocket STOMP

| Topic | Trigger | Payload |
|-------|---------|---------|
| `/topic/orders/{orderId}/status` | Order status updated | `{ orderId, referenceNumber, previousStatus, newStatus, timestamp }` |
| `/user/{userId}/queue/notifications` | Same trigger | `{ type, orderId, newStatus, message, timestamp }` |

- JWT required at WebSocket handshake (Authorization header or `?token=` query param)
- Unauthenticated connections rejected with `UNAUTHORIZED`

#### FR-7.4 Acceptance Criteria
- [ ] Both pagination modes handle all edge cases
- [ ] Product search Specification works with any filter combination
- [ ] WebSocket notifications fire on order status change
- [ ] JWT enforced on WebSocket handshake
- [ ] At least **10 new tests**

---

### Phase 8 — Email Notifications & Production Readiness

**Topics:** Spring Mail, Actuator, Micrometer, Logback, Compression, Graceful Shutdown  
**Minimum Tests:** 7

#### FR-8.1 Email Templates (Greenmail-mocked in tests)

| # | Trigger | Recipient | Content |
|---|---------|-----------|---------|
| 16 | User signup | New user | HTML welcome + email verification link |
| 17 | Forgot password | User | HTML reset link (time-limited token) |
| 18 | Order CONFIRMED | Customer | HTML itemized order summary |
| 19 | Stock drops below threshold after fulfillment | Admins | HTML low-stock alert |
| 20 | Daily digest (`@Scheduled`) or manual admin trigger | Admins | HTML order summary for the day |

#### FR-8.2 Production Hardening

| Requirement | Detail |
|-------------|--------|
| Request logging | `RequestLoggingFilter` with MDC trace ID — fully operational |
| GZIP compression | `server.compression.enabled=true`, min response size configurable |
| Actuator endpoints | `health`, `info`, `metrics`, `prometheus` exposed; auth required in prod |
| Custom Micrometer metrics | `orders.placed.count`, `orders.revenue.total`, `orders.value.average` |
| Profiles | `application-dev.yml`, `application-test.yml`, `application-prod.yml` — no hardcoded secrets |
| Graceful shutdown | `server.shutdown=graceful` — drain in-flight requests before closing |

#### FR-8.3 Acceptance Criteria
- [ ] All 5 email types sent via Greenmail mock in tests (no real SMTP)
- [ ] Production middleware fully operational (all 3 filters)
- [ ] Actuator accessible; custom Micrometer counters increment correctly
- [ ] Full E2E integration test: signup → login → create product (admin) → place order → change status → verify email sent
- [ ] JaCoCo coverage **> 80%** for all metrics
- [ ] Total test count: **>= 80 tests**

---

## 8. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|------------|
| NFR-1 | Performance | API response time < 500ms under normal load |
| NFR-2 | Observability | Every request logged with trace ID; structured Logback format |
| NFR-3 | Security | No plaintext passwords; no secrets in git; JWT expiry enforced |
| NFR-4 | Rate Limiting | Max 5 auth req/15 min per IP (Bucket4j) |
| NFR-5 | Scalability | Stateless API; horizontal scaling capable |
| NFR-6 | Maintainability | SOLID principles; DRY; no business logic in controllers |
| NFR-7 | Reliability | Graceful shutdown; atomic stock operations; no partial deductions |
| NFR-8 | Portability | Docker Compose provided; Flyway ensures reproducible DB from scratch |
| NFR-9 | CI/CD | GitHub Actions pipeline; linting + 80% coverage gate |
| NFR-10 | Code Quality | No `@Disabled` tests; no trivial `assertTrue(true)` |

---

## 9. Complete API Endpoint Catalogue

| Method | Path | Auth | Role | Phase |
|--------|------|------|------|-------|
| GET | `/api/health` | None | Public | 1 |
| GET | `/actuator/health` | None | Public | 1 |
| GET | `/actuator/metrics` | Bearer | ADMIN | 8 |
| GET | `/actuator/prometheus` | Bearer | ADMIN | 8 |
| POST | `/api/auth/signup` | None | Public | 4 |
| POST | `/api/auth/login` | None | Public | 4 |
| POST | `/api/auth/refresh` | None | Public | 4 |
| POST | `/api/auth/forgot-password` | None | Public | 4 |
| POST | `/api/auth/reset-password/{token}` | None | Public | 4 |
| GET | `/api/auth/me` | Bearer | USER/ADMIN | 4 |
| PUT | `/api/auth/me/avatar` | Bearer | USER/ADMIN | 6 |
| GET | `/api/products` | None | Public | 3 |
| POST | `/api/products` | Bearer | ADMIN | 3 |
| GET | `/api/products/{id}` | None | Public | 3 |
| PUT | `/api/products/{id}` | Bearer | ADMIN | 3 |
| PATCH | `/api/products/{id}/stock` | Bearer | ADMIN | 3 |
| DELETE | `/api/products/{id}` | Bearer | ADMIN | 3 |
| POST | `/api/products/{id}/image` | Bearer | ADMIN | 6 |
| GET | `/api/products/{id}/image` | None | Public | 6 |
| POST | `/api/orders` | Bearer | USER | 3 |
| GET | `/api/orders` | Bearer | USER/ADMIN | 3 |
| GET | `/api/orders/{id}` | Bearer | USER/ADMIN | 3 |
| PATCH | `/api/orders/{id}/status` | Bearer | ADMIN | 3 |
| GET | `/api/orders/{id}/report` | Bearer | USER/ADMIN | 6 |
| GET | `/api/orders/export` | Bearer | ADMIN | 6 |
| GET | `/api/admin/products/low-stock` | Bearer | ADMIN | 7 |

---

## 10. Security Requirements

| Requirement | Specification |
|-------------|--------------|
| Password hashing | BCrypt, strength >= 10 |
| JWT access token | Short-lived (15 min default); HS256 signed |
| JWT refresh token | Long-lived (7 days); stored securely |
| Password reset token | Single-use, time-limited (1-24 hours), cleared after use |
| Auth rate limiting | 5 requests / 15 min per IP on `/api/auth/**` (MW-2) |
| Request trace ID | UUID per request, in every log line and `X-Trace-Id` header (MW-1) |
| CSRF | Disabled (stateless JWT API) |
| CORS | Configured per profile — wildcard in dev, restricted in prod |
| WebSocket auth | JWT required at handshake; reject unauthenticated connections |
| Actuator | Require ADMIN role in prod profile |
| Secrets | Zero secrets in source code or git history |
| Stack traces | Suppressed in `prod` profile; visible in `dev` profile |

---

## 11. Testing Requirements

### Test Count Targets

| Phase | Unit | Integration/Repository | Minimum |
|-------|------|----------------------|---------|
| 1 | 3 | 4 | **6** |
| 2 | 7 | 8 | **15** |
| 3 | 6 | 10 | **20** |
| 4 | 8 | 8 | **14** |
| 5 | 5 | 8 | **12** |
| 6 | 4 | 4 | **8** |
| 7 | 4 | 6 | **10** |
| 8 | 4 | 4 | **7** |
| **Total** | **41+** | **52+** | **>= 80** |

### Mandatory Middleware Tests (included in Phase 1 & 4 counts)

| Test | Type |
|------|------|
| `requestLoggingFilter_anyRequest_addsXTraceIdHeader` | Unit |
| `requestLoggingFilter_afterRequest_mdcIsCleared` | Unit |
| `rateLimitingFilter_exceedLimit_returns429` | Unit |
| `rateLimitingFilter_nonAuthRoute_bypasses` | Unit |
| `jwtFilter_validToken_setsSecurityContext` | Unit |
| `jwtFilter_missingHeader_passesThrough` | Unit |
| `jwtFilter_expiredToken_returns401` | Unit |
| `jwtFilter_malformedToken_returns401` | Unit |

### Testing Standards
- `@ExtendWith(MockitoExtension.class)` for unit tests
- `@DataJpaTest` + Testcontainers for repository tests
- `@SpringBootTest` + Testcontainers + `AbstractIntegrationTest` for integration tests
- No `@Disabled` tests
- No `assertTrue(true)` or equivalent trivial assertions
- No commented-out test code
- `ArgumentCaptor` used to verify exact values passed to collaborators

---

## 12. Delivery & Submission Checklist

### Repository
- [ ] GitHub repo: https://github.com/tejash-sr/StoreFlowAPI
- [ ] Clean incremental commit history (at least one commit per phase)
- [ ] `README.md` accurately covers setup from scratch
- [ ] `application-example.yml` committed with placeholder values (no real secrets)
- [ ] `.gitignore` excludes `target/`, IDE files, `*.env`, `uploads/`
- [ ] No build artifacts committed

### Working Application
- [ ] `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` starts successfully
- [ ] `docker-compose up` starts full stack (app + DB) cleanly from scratch
- [ ] `Dockerfile` present and working
- [ ] All Flyway migrations apply automatically on first startup
- [ ] Postman collection at `postman/StoreFlowAPI.postman_collection.json`
- [ ] All 3 middleware filters active and verifiable via logs/headers

### Test Suite
- [ ] `./mvnw test` → **0 failures, 0 errors**
- [ ] `./mvnw verify` → JaCoCo **>= 80%** for lines, branches, methods, instructions
- [ ] JaCoCo HTML report at `target/site/jacoco/index.html`
- [ ] **>= 80 total tests** (unit + integration; no trivial tests)
- [ ] Middleware filter tests included

### CI/CD
- [ ] `.github/workflows/ci.yml` present and passing
- [ ] Pipeline: build → lint → test → coverage check on every push to `main`

### Documentation
- [ ] All 5 docs in `docs/` present and up to date
- [ ] `docs/PRD.md` — functional requirements
- [ ] `docs/IMPLEMENTATION.md` — implementation guide
- [ ] `docs/DESIGN.md` — architecture and design
- [ ] `docs/TESTING_GUIDE.md` — testing strategy and checklist
- [ ] `docs/API_REFERENCE.md` — API endpoint reference

---

## 13. Evaluation Rubric

| Criteria | Weight | What Reviewers Look For |
|----------|--------|------------------------|
| **Test Quality** | 30% | Meaningful assertions, edge cases, Mockito mocking/stubbing, no false positives, middleware tested |
| **Code Architecture** | 25% | Clean layers, DTO separation, DRY, SOLID, middleware in correct package |
| **API Design** | 20% | RESTful conventions, consistent error envelopes, correct HTTP codes, pagination |
| **Feature Completeness** | 15% | All 8 phases implemented; Flyway applies cleanly; middleware fully functional |
| **Error Handling** | 10% | GlobalExceptionHandler covers all types; structured JSON errors; no unexpected 500s |

---

## 14. Bonus Challenges

### Challenge A — GraphQL Layer
- Spring for GraphQL running alongside REST
- Query/Mutation types for products, orders, users
- Auth in GraphQL context via `HandlerInterceptor`
- `@GraphQLTest` slice tests for resolvers

### Challenge B — Background Job Processing
- Email via `@Async` + `ThreadPoolTaskExecutor`
- `@Scheduled` daily digest
- Async PDF generation with job-ID + polling endpoint
- Synchronous executor in test profile

### Challenge C — OpenAPI / Swagger UI
- SpringDoc OpenAPI: `springdoc-openapi-starter-webmvc-ui`
- `@Operation`, `@ApiResponse`, `@Schema` on all controllers/DTOs
- Bearer auth in Swagger UI (Authorize button)
- Accessible at `/swagger-ui.html`
