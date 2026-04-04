## Page 1

StoreFlow API – Comprehensive Exercise Grootan Technologies Internal Training

# COMPREHENSIVE EXERCISE
# StoreFlow API
*A Production-Grade Inventory & Order Management REST API*

---

## Courses Covered:
* Spring Framework 6: Beginner to Guru
* Spring Boot Unit Testing with JUnit, Mockito & Testcontainers
* PostgreSQL Bootcamp: Complete Beginner to Advanced

## Grootan Technologies – Internal Training Program

Confidential – Internal Use Only &lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 2

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Exercise Overview

You will build StoreFlow API – a complete inventory and order management REST API using Spring Boot 3, Spring Data JPA, PostgreSQL, and Spring Security with JWT authentication. The project is structured into 8 progressive phases, each targeting specific topics from all three courses. Every phase requires comprehensive unit and integration tests written with JUnit 5 and Mockito before or alongside the implementation code (TDD encouraged).

## Goal

Build a fully tested, production-quality REST API that covers 90%+ of concepts from all three courses. Each phase has acceptance criteria – your tests must pass before moving to the next phase.

## Tech Stack

<table>
<thead>
<tr>
<th>Category</th>
<th>Technology</th>
</tr>
</thead>
<tbody>
<tr>
<td>Runtime</td>
<td>Java 21 + Spring Boot 3.x</td>
</tr>
<tr>
<td>Framework</td>
<td>Spring MVC + Spring Data JPA + Hibernate</td>
</tr>
<tr>
<td>Database</td>
<td>PostgreSQL 15 with Flyway migrations</td>
</tr>
<tr>
<td>Authentication</td>
<td>Spring Security + JWT (JWT) + BCrypt</td>
</tr>
<tr>
<td>Testing</td>
<td>JUnit 5 + Mockito + Testcontainers + MockMvc</td>
</tr>
<tr>
<td>Validation</td>
<td>Jakarta Bean Validation + custom validators</td>
</tr>
<tr>
<td>File Handling</td>
<td>Spring Multipart + iText / Apache PDFBox (reports)</td>
</tr>
<tr>
<td>Email</td>
<td>Spring Mail (JavaMailSender) with test stubs</td>
</tr>
<tr>
<td>Real-time</td>
<td>Spring WebSocket + STOMP (for notifications)</td>
</tr>
<tr>
<td>Build Tool</td>
<td>Maven or Gradle (your choice)</td>
</tr>
</tbody>
</table>

## Project Structure

Organize your project following standard Spring Boot layered architecture. You should have separate packages for: config (security, CORS, Swagger), controllers (thin request handlers annotated with @RestController), services (business logic – the main testable units), repositories (Spring Data JPA interfaces), models / entities (JPA entity classes), dto (request and response DTOs with validation annotations), middleware / filters (JWT filter, error handling), utils (PDF generator, email builder, helpers), and exceptions (custom exception classes with @ControllerAdvice handler). Keep your application context separate from your server startup so the Spring application context can be loaded in tests without binding to a port.

## Course Topic Mapping

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 3

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

Each phase maps directly to specific sections of all three courses:

<table>
  <thead>
    <tr>
      <th>Phase</th>
      <th>Spring Boot Course Topics</th>
      <th>Testing Course Topics</th>
      <th>Deliverable</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>1</td>
      <td>Spring Boot project setup, MVC, application.yml</td>
      <td>JUnit 5, @SpringBootTest, MockMvc setup</td>
      <td>Project scaffold + first tests</td>
    </tr>
    <tr>
      <td>2</td>
      <td>JPA entities, Hibernate, PostgreSQL schemas, Flyway</td>
      <td>Repository tests, @DataJpaTest, Testcontainers</td>
      <td>Data models + full CRUD repository tests</td>
    </tr>
    <tr>
      <td>3</td>
      <td>REST controllers, @RequestBody, ResponseEntity</td>
      <td>Integration tests, MockMvc, AAA pattern</td>
      <td>Complete CRUD endpoints + API tests</td>
    </tr>
    <tr>
      <td>4</td>
      <td>Spring Security, JWT, filters, role-based access</td>
      <td>Spying, @BeforeEach hooks, security testing</td>
      <td>Auth system with protected routes</td>
    </tr>
    <tr>
      <td>5</td>
      <td>Bean Validation, @ExceptionHandler, error responses</td>
      <td>Testing error paths, edge cases</td>
      <td>Robust validation + global error handling</td>
    </tr>
    <tr>
      <td>6</td>
      <td>Multipart file upload, streaming, PDF generation</td>
      <td>Mocking file I/O, stream testing</td>
      <td>File attachments + PDF export</td>
    </tr>
    <tr>
      <td>7</td>
      <td>Advanced JPQL, pagination, WebSockets (STOMP)</td>
      <td>Testing pagination, socket events</td>
      <td>Advanced queries + real-time</td>
    </tr>
    <tr>
      <td>8</td>
      <td>Spring Mail, Actuator, production hardening</td>
      <td>E2E-style tests, coverage reports</td>
      <td>Email + production readiness + 80% coverage</td>
    </tr>
  </tbody>
</table>

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 4

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Phase 1: Project Foundation & Spring Boot Setup

Course Topics: Spring Boot Basics, Spring MVC, Application Configuration, JUnit 5 Setup, MockMvc Configuration

## Objective
Set up a Spring Boot project with PostgreSQL, configure JUnit 5 with Testcontainers, implement health-check endpoints, and establish the layered package structure. Write your first unit and integration tests.

### 1.1 – Project Setup
Initialize a Spring Boot 3 project with the following dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL Driver, Flyway Migration, Spring Validation, Lombok, and Spring Boot Actuator. Configure your application using `application.yml` (not `application.properties`). Separate your application entry point from your configuration beans so the application context can be loaded in tests independently.

### 1.2 – Test Infrastructure Configuration
* Use JUnit 5 as the test runner; disable JUnit 4 vintage engine
* Configure Testcontainers to spin up a real PostgreSQL instance for integration tests
* Set up a global test base class annotated with `@SpringBootTest` and `@Testcontainers` that all integration tests extend
* Configure MockMvc for controller layer testing (without full Spring context where appropriate)
* Enable JaCoCo code coverage with 80% threshold enforced at build time for branches, methods, lines, and instructions

### 1.3 – Initial Endpoints & Middleware
Implement the following:
* Health endpoint (GET /api/health): Return the application status, current timestamp, and JVM uptime as a JSON response.
* 404 fallback: Any unmatched route should return 404 with a consistent error response body.
* Global exception handler: A `@ControllerAdvice` class that intercepts all uncaught exceptions and returns a structured JSON error response with the correct HTTP status.
* Security baseline: Apply CORS configuration and disable CSRF for stateless API usage. All routes are publicly accessible for now (auth comes in Phase 4).

### 1.4 – Required Tests
#### Unit Tests:

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 5

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

*   Test that the global exception handler returns HTTP 500 for generic RuntimeException and preserves custom status codes for domain exceptions
*   Test that unknown errors are serialized with a consistent JSON shape (fields: timestamp, status, message, path)
*   Test a utility class (e.g., a date formatter or response builder) with multiple input scenarios

**Integration Tests:**
*   GET /api/health returns HTTP 200 with correct response shape (status, timestamp, uptime)
*   GET /api/nonexistent returns HTTP 404 with error message
*   Verify CORS headers are present on responses when Origin header is sent
*   Verify Actuator health endpoint is accessible at /actuator/health

☑ **Acceptance Criteria**
All tests pass. Spring context loads without errors. Application starts with a single command.
At least 6 passing tests covering both unit and integration layers.

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 6

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Phase 2: Data Models & Database Layer

Course Topics: JPA Entities, Hibernate Relationships, PostgreSQL Schema Design, Flyway Migrations, NoSQL vs SQL Concepts, @DataJpaTest, Testcontainers

## Objective
Design and implement JPA entity models for Users, Products, Categories, Orders, and OrderItems with proper Hibernate relationships and Flyway migration scripts. Write comprehensive repository tests using @DataJpaTest and Testcontainers.

## 2.1 – User Entity
Design a User entity with the following fields: email (unique, required, valid format), password (required, min 8 chars, stored hashed), fullName (required, 2-100 chars), role (enum: USER/ADMIN – default USER), optional avatarPath, optional resetToken and resetTokenExpiresAt for password recovery, enabled flag, and audit timestamps (createdAt, updatedAt managed by @EntityListeners).
Implement: a transient helper method isResetTokenValid() that returns whether the reset token is non-null and not expired. The password field must never be serialized in JSON responses (use @JsonIgnore or a DTO approach).

## 2.2 – Product & Category Entities
Design a Category entity with: name (unique, required), description, optional parent Category (self-referencing for subcategories), and status. Design a Product entity with: name (required, 3-150 chars), description (required, max 3000 chars), sku (unique, required), price (required, positive), stockQuantity (non-negative integer), category (ManyToOne), optional imageUrl, status (enum: ACTIVE/INACTIVE/DISCONTINUED), and audit timestamps.
Implement custom repository methods: findBySkuIgnoreCase, findByCategory, and findAllByStockQuantityLessThan(int threshold) for low-stock alerts.

## 2.3 – Order & OrderItem Entities
Design an Order entity with: reference number (auto-generated, unique), customer (ManyToOne User), orderItems (OneToMany, cascade ALL), status (enum: PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED), shippingAddress (embeddable value object with street, city, country, postalCode), totalAmount (derived or stored), and timestamps. Design an OrderItem entity with: order (ManyToOne), product (ManyToOne), quantity (positive int), unitPrice (snapshot of price at order time), and subtotal. Add Flyway migration scripts (V1, V2, V3...) for each schema change.

## 2.4 – Required Tests
Test each entity and repository in isolation using @DataJpaTest and Testcontainers:

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 7

StoreFlow API – Comprehensive Exercise Grootan Technologies Internal Training

*   User: validation passes/fails for email format, password length, name constraints
*   User: email uniqueness constraint is enforced at the database level
*   Product: SKU uniqueness is enforced; price and stockQuantity reject invalid values
*   Product: findAllByStockQuantityLessThan returns only products below the threshold
*   Order: cascade operations correctly persist OrderItems when an Order is saved
*   Order: total amount is consistent with the sum of OrderItem subtotals
*   Category: self-referencing parent/child relationship is correctly persisted and loaded
*   Flyway: all migration scripts apply cleanly in order on a fresh schema

☑ **Acceptance Criteria**
All 5 entities implemented with proper JPA annotations and TypeScript-equivalent DTOs.
Flyway migrations version-controlled. At least 15 repository/entity tests passing against a real PostgreSQL instance via Testcontainers.

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 8

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Phase 3: REST API Endpoints & CRUD

Course Topics: Spring MVC REST Controllers, @RequestBody, ResponseEntity, Service Layer Pattern, MockMvc Integration Testing

## Objective
Build complete CRUD REST endpoints for Products, Categories, and Orders following the service layer pattern. Controllers must be thin – they parse the request and delegate to a @Service. The service layer contains all business logic and is the primary target for unit tests. Write MockMvc integration tests for every endpoint.

## 3.1 – API Endpoints
1. POST /api/products – Create a new product
2. GET /api/products – List products with optional query filters (category, status, minPrice, maxPrice) and pagination
3. GET /api/products/:id – Get a single product with its category details
4. PUT /api/products/:id – Full update of a product
5. PATCH /api/products/:id/stock – Partial update: adjust stock quantity (increment or decrement)
6. DELETE /api/products/:id – Soft-delete product (set status to DISCONTINUED and mark deletedAt)
7. POST /api/orders – Place a new order (validate stock availability, deduct stock, calculate total)
8. GET /api/orders – List orders for the authenticated user (admin sees all)
9. GET /api/orders/:id – Get a single order with all items and product details
10. PATCH /api/orders/:id/status – Update order status (admin only, must follow valid status transitions)

## 3.2 – Required Tests
### Unit Tests (mock the repository/service layer with Mockito):
*   ProductService.create – verifies correct data flows to the repository
*   ProductService.create – throws ResourceNotFoundException when category is not found
*   OrderService.placeOrder – throws InsufficientStockException when a product has insufficient stock
*   OrderService.placeOrder – correctly deducts stock for each OrderItem
*   OrderService.updateStatus – throws InvalidStatusTransitionException for illegal state changes
*   ProductService.delete – sets status to DISCONTINUED instead of deleting the database row

### Integration Tests (MockMvc + Testcontainers):

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 9

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

*   POST /api/products with valid data returns HTTP 201 and the created product DTO
*   POST /api/products with missing required fields returns HTTP 400 with error details
*   GET /api/products returns paginated list with correct pagination metadata (page, size, totalElements, totalPages)
*   GET /api/products?category=Electronics filters results by category correctly
*   GET /api/products/:id with a valid ID returns the product with category populated
*   GET /api/products/:id with a non-existent ID returns HTTP 404
*   PUT /api/products/:id updates all fields correctly and returns the updated DTO
*   PATCH /api/products/:id/stock correctly adjusts quantity and rejects negative results
*   POST /api/orders places the order, deducts stock, and returns HTTP 201 with the order reference
*   POST /api/orders fails with HTTP 409 when any item has insufficient stock

☑ **Acceptance Criteria**
All endpoints functional. At least 20 tests passing (6+ unit, 10+ integration). Service layer fully separated from controllers. Pagination works with page/size query params. Stock management is atomic.

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 10

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Phase 4: Authentication & Authorization

Course Topics: Spring Security, JWT, SecurityFilterChain, Method-level Security, Password Reset Flow, Mockito Spying, @BeforeEach Hooks

## Objective
Implement a complete authentication system with JWT access tokens, refresh tokens, and a password reset flow. Add role-based authorization to protect routes. Update all Phase 3 tests to include Authorization headers.

## 4.1 – Auth Endpoints

*   POST /api/auth/signup – Register a new user, hash password with BCrypt, return JWT
*   POST /api/auth/login – Verify credentials, return access token + refresh token
*   POST /api/auth/refresh – Issue a new access token using a valid refresh token
*   POST /api/auth/forgot-password – Generate a reset token, send password reset email
*   POST /api/auth/reset-password/{token} – Reset password using a valid, non-expired token
*   GET /api/auth/me – Get the current user's profile (protected route)

## 4.2 – Security Configuration & Authorization

*   JwtAuthenticationFilter: A OncePerRequestFilter that extracts the Bearer token, validates it, and sets the authentication in the SecurityContext
*   Role-based access: ADMIN role required for product creation/update/deletion and order status changes; USER role can place orders and read products
*   Method-level security: Use @PreAuthorize where appropriate to enforce ownership (users can only access their own orders)
*   Rate limiting: Apply request-rate limiting to auth endpoints (e.g., 5 requests per 15 minutes per IP using a Bucket4j or similar approach)

## 4.3 – Required Tests

### Unit Tests:

*   JwtAuthenticationFilter: passes the request when a valid JWT is present in the Authorization header
*   JwtAuthenticationFilter: returns HTTP 401 for a missing token, an expired token, and a malformed token
*   AuthService.signup: hashes the password with BCrypt and returns a JWT
*   AuthService.login: throws AuthenticationException for wrong email and wrong password as separate test cases
*   AuthService.resetPassword: throws AppException with HTTP 400 for an expired token and for an invalid token separately

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 11

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

* AuthService.forgotPassword: calls the email service exactly once with a reset link containing the correct token

**Integration Tests:**
* Full signup flow: POST /api/auth/signup returns a JWT → use that JWT to GET /api/auth/me successfully
* POST /api/auth/login with correct credentials returns access token and refresh token
* POST /api/auth/login with wrong password returns HTTP 401
* Accessing a protected endpoint without a token returns HTTP 401
* Accessing an ADMIN-only endpoint as a USER role returns HTTP 403
* Full password reset flow: forgot-password → use token from email stub → reset-password → login with new password
* POST /api/auth/refresh with a valid refresh token returns a new access token

☑ **Acceptance Criteria**
Complete auth system with JWT access + refresh tokens. All Phase 3 integration tests updated to include a valid Authorization header. Password reset flow works end-to-end with a mocked email transport. At least 14 new auth tests passing.

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 12

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Phase 5: Validation & Error Handling

**Course Topics:** Jakarta Bean Validation, Custom ConstraintValidator, @ExceptionHandler, @ControllerAdvice, HTTP Status Codes, Error Response Design

## Objective
Add thorough input validation to every endpoint using Jakarta Bean Validation annotations and custom validators. Implement a custom exception hierarchy and a centralized @ControllerAdvice that gracefully handles all error types with consistent response shapes.

## 5.1 – Validation Rules

*   Implement Jakarta Bean Validation annotations (@NotBlank, @Size, @Email, @Positive, etc.) on all request DTOs
*   Product name: required, trimmed, 3-150 characters
*   Product price: required, positive decimal value; stockQuantity: non-negative integer
*   SKU: required, must match a defined pattern (alphanumeric + hyphens, uppercase)
*   Category ID in product requests: must reference an existing category (implement a custom @ExistsInDatabase ConstraintValidator)
*   Order quantity: positive integer per item; shipping address fields: all required, postalCode matches country-specific patterns
*   Email fields: standard email format, normalized to lowercase before persistence
*   Sanitize all string inputs: strip leading/trailing whitespace via @Trimmed or service-layer normalization

## 5.2 – Custom Exception System

*   Create a base AppException class with message, statusCode (HttpStatus), and an optional Map of field-level errors
*   Define domain exceptions extending AppException: ResourceNotFoundException (404), InsufficientStockException (409), InvalidStatusTransitionException (422), AuthenticationFailedException (401), AccessDeniedException (403)
*   Global @ControllerAdvice: catches all exceptions and returns a consistent JSON error response with fields: timestamp, status, error, message, path, and optional errors map for field-level errors
*   Handle MethodArgumentNotValidException (Bean Validation failure) → return HTTP 400 with a map of field names to error messages
*   Handle DataIntegrityViolationException (unique constraint violation) → return HTTP 409 with the conflicting field identified
*   Handle JwtException (expired or invalid token) → return HTTP 401
*   Suppress stack traces in production profile; include them in the development profile

## 5.3 – Required Tests

<table>
<thead>
<tr>
<th>Test Description</th>
<th>Expected Outcome</th>
<th>Test Cases</th>
</tr>
</thead>
<tbody>
<tr>
<td>Validation of Product Name</td>
<td>Product name is required, trimmed, and within the valid length range.</td>
<td>Test cases with valid and invalid product names.</td>
</tr>
<tr>
<td>Validation of Product Price</td>
<td>Product price is required, positive, and within the valid range.</td>
<td>Test cases with valid and invalid product prices.</td>
</tr>
<tr>
<td>Validation of SKU</td>
<td>SKU is required, alphanumeric with hyphens, and in uppercase.</td>
<td>Test cases with valid and invalid SKUs.</td>
</tr>
<tr>
<td>Validation of Category ID</td>
<td>Category ID exists in the database and references an existing category.</td>
<td>Test cases with valid and invalid category IDs.</td>
</tr>
<tr>
<td>Validation of Order Quantity</td>
<td>Order quantity is a positive integer.</td>
<td>Test cases with valid and invalid order quantities.</td>
</tr>
<tr>
<td>Validation of Shipping Address Fields</td>
<td>Shipping address fields are required and match country-specific patterns.</td>
<td>Test cases with valid and invalid shipping addresses.</td>
</tr>
<tr>
<td>Validation of Email Fields</td>
<td>Email fields have standard format and are normalized to lowercase.</td>
<td>Test cases with valid and invalid email addresses.</td>
</tr>
<tr>
<td>Custom Exception Handling</td>
<td>Custom exceptions are handled correctly and return appropriate HTTP status codes with detailed error messages.</td>
<td>Test cases for each custom exception type.</td>
</tr>
</tbody>
</table>

&lt;watermark&gt;Confidential – Internal Use Only&lt;/watermark&gt;
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 13

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

*   Unit test each custom ConstraintValidator (e.g., @ExistsInDatabase, SKU pattern validator)
*   Unit test AppException subclasses: verify they carry the correct HttpStatus and message
*   Unit test @ControllerAdvice handler: MethodArgumentNotValidException maps to HTTP 400 with field-level error map
*   Unit test @ControllerAdvice handler: DataIntegrityViolationException maps to HTTP 409
*   Unit test @ControllerAdvice handler: JwtException maps to HTTP 401
*   Integration test: POST endpoints with each type of invalid field return the specific field error message
*   Integration test: verify that duplicate SKU on product creation returns HTTP 409 with the conflicting field name
*   Integration test: verify correct HTTP status codes for all error scenarios (400, 401, 403, 404, 409, 422, 500)

☑ **Acceptance Criteria**
Every endpoint has validation annotations. Custom exception hierarchy is used throughout the service layer. @ControllerAdvice covers all error types. At least 12 new tests. No unhandled exceptions reach the client as 500 errors unexpectedly.

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 14

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Phase 6: File Upload, Download & PDF Generation

Course Topics: Spring Multipart, InputStreamResource, PDF Generation (iText / Apache PDFBox), Streaming Responses, Mocking File I/O

## Objective
Implement file upload/download for product images and user avatars, generate on-demand order summary PDFs, and export order data as CSV files. All file operations must be tested with mocked or in-memory file I/O.

## 6.1 – Features

11. **Product Image Upload** (POST `/api/products/:id/image`): Accept image via multipart/form-data. Max 5MB, allowed types: `image/jpeg`, `image/png`, `image/webp`. Store file in a configurable location; persist the relative path in the product's `imageUrl` field.
12. **Image Download** (GET `/api/products/:id/image`): Stream the image file back to the client with the correct Content-Type and Content-Disposition headers.
13. **User Avatar** (PUT `/api/auth/me/avatar`): Upload and optionally resize the profile image using a library such as Thumbnailator before storing.
14. **Order Summary PDF** (GET `/api/orders/:id/report`): Generate a PDF on-the-fly containing: order reference, customer name, order date, itemized line items with prices, and order total. Stream it as `application/pdf` with an appropriate Content-Disposition filename.
15. **Order Export CSV** (GET `/api/orders/export?from=&to=`): Export all orders within a date range as a downloadable CSV file with a header row and one row per OrderItem.

## 6.2 – Required Tests

*   Unit test: file upload validation rejects files larger than 5MB and returns HTTP 400
*   Unit test: file upload validation rejects disallowed MIME types (e.g., `application/pdf` for the image endpoint)
*   Unit test: PDF generation service produces a non-empty byte array containing the expected order reference text
*   Unit test: CSV generation service produces a correctly formatted header row and one data row per OrderItem
*   Integration test: upload a product image, then download it and verify the response body and Content-Type header match
*   Integration test: uploading an oversized file returns HTTP 400 with an appropriate error message
*   Integration test: GET `/api/orders/:id/report` returns Content-Type: `application/pdf`
*   Integration test: upload avatar, then GET `/api/auth/me` returns a non-null avatarUrl

☑ **Acceptance Criteria**

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 15

StoreFlow API – Comprehensive Exercise Grootan Technologies Internal Training

File upload/download works with proper size and type validation. PDF report generates dynamically and streams correctly. CSV export is functional. At least 8 new tests.

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 16

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Phase 7: Advanced Queries, Pagination & Real-time

Course Topics: JPQL, Native Queries, Specifications, Pageable, Sorting, Spring WebSocket, STOMP, Testing Async Operations

## Objective
Implement both offset-based and cursor-based pagination with multi-field sorting. Add advanced PostgreSQL-backed search using Specifications or JPQL. Integrate Spring WebSocket with STOMP so relevant users receive real-time notifications when order statuses change.

## 7.1 – Advanced Pagination, Sorting & Querying
* Implement both offset-based (page/size) and cursor-based (cursor/size) pagination on the product listing and order listing endpoints
* Offset response includes: content, totalElements, totalPages, page, size, first, last, hasNext
* Cursor response includes: content, nextCursor, hasMore, size
* Handle edge cases: page beyond last, empty result set, size of 0 or negative (default to 20, cap at 100)
* Add multi-field sorting: sort by createdAt, price, name, stockQuantity via sort query parameters
* Implement a product search endpoint using JPA Specifications or a JPQL query that filters by name (partial, case-insensitive), category, price range, and status in a single flexible query
* Add a low-stock report endpoint (GET /api/admin/products/low-stock?threshold=10) using a custom JPQL query

## 7.2 – Real-time Notifications (WebSocket + STOMP)
* Configure Spring WebSocket with STOMP and SockJS fallback
* When an order's status changes, emit a STOMP message to the topic /topic/orders/{orderId}/status containing the new status and timestamp
* Additionally, emit a message to /user/{userId}/queue/notifications for personal user notifications
* Auth: WebSocket handshake must include a valid JWT in the Authorization header or as a query parameter; reject unauthenticated connections with UNAUTHORIZED

## 7.3 – Required Tests
* Unit test: cursor-based pagination utility returns correct nextCursor and hasMore values for various dataset sizes
* Unit test: offset pagination correctly calculates totalPages for 0 items, exact multiples, and partial pages

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 17

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

*   Unit test: the WebSocket notification service is invoked with the correct topic and payload when order status changes
*   Unit test: WebSocket authentication interceptor rejects connections without a valid JWT
*   Integration test: product search with combined name, category, and price range filters returns correct results
*   Integration test: pagination with various page/size combinations returns correct data slices with accurate metadata
*   Integration test: cursor-based pagination can traverse the full dataset without duplicates or gaps
*   Integration test: low-stock report returns only products below the specified threshold
*   Integration test: WebSocket client receives order status notification after PATCH /api/orders/:id/status

☑ **Acceptance Criteria**
Both pagination modes work correctly and handle edge cases. Product search Specification works with any combination of filters. WebSocket notifications fire for order status changes. Auth enforced on WebSocket connections. At least 10 new tests.

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 18

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Phase 8: Email Notifications & Production Readiness

Course Topics: Spring Mail, JavaMailSender, Spring Actuator, Micrometer, Logging (Logback), Compression, Coverage Reports

## Objective
Add transactional email notifications using Spring Mail, apply production-hardening configuration, expose operational metrics via Actuator, and ensure the full test suite achieves 80%+ code coverage across all metrics.

## 8.1 – Email Notifications

Using JavaMailSender (with a Greenmail or mock JavaMailSender in the test profile so no real emails are sent), implement HTML emails for:

16. Welcome email on signup (with an email verification link)
17. Password reset email (containing the time-limited reset token link)
18. Order confirmation email (sent to the customer when an order is CONFIRMED, containing the itemized summary)
19. Low-stock alert email (sent to admins when a product's stockQuantity drops below a configured threshold after an order is fulfilled)
20. Daily order digest (a summary of orders placed that day, designed to be triggered by a scheduled @Scheduled method or a manual admin endpoint)

## 8.2 – Production Hardening

*   Add structured request/response logging via a custom OncePerRequestFilter or use Logback with MDC to include a request-scoped trace ID (UUID) in every log line and response header
*   Add response compression (GZIP) for responses above a configurable size threshold
*   Configure Spring Boot Actuator to expose health, info, metrics, and prometheus endpoints under /actuator with authentication protection
*   Expose custom Micrometer metrics: order placement count, total revenue counter, and average order value gauge
*   Environment-specific configuration using Spring Profiles (application-dev.yml, application-test.yml, application-prod.yml) – no hardcoded credentials anywhere
*   Graceful shutdown: configure Spring Boot's built-in graceful shutdown to drain in-flight requests before closing the JPA context and WebSocket connections

## 8.3 – Required Tests

*   Unit test: email service composes the correct HTML body, subject line, and recipient for each of the 5 email types
*   Unit test: email service uses the mock/Greenmail transport in the test profile and never invokes a real SMTP server

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 19

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

*   Unit test: low-stock alert logic correctly identifies products below the threshold after stock deduction
*   Integration test: full user journey – signup → login → create product (admin) → place order → update order status → verify order confirmation email was sent via mock transport
*   Integration test: Actuator /actuator/health returns UP status with database connectivity confirmed
*   Integration test: custom Micrometer counter increments after each successful order placement
*   Verify total JaCoCo coverage report shows above 80% for lines, branches, methods, and instructions

☑ **Acceptance Criteria**
Emails are sent via mocked transport in tests. Production middleware and Actuator configured. Full user journey integration test passes. Overall test coverage > 80%. Total test count across all phases: 80+.

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 20

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Bonus Challenges (Optional)

For those who want to push further after completing all 8 phases:

## Challenge A: GraphQL Layer

Add a Spring for GraphQL API alongside the REST API (both should work simultaneously):
* Implement Query types for products, orders, and users with field-level resolvers
* Implement Mutations for CRUD operations with input validation
* Add authentication to the GraphQL context using a custom HandlerInterceptor
* Write @GraphQLTest slice tests for resolvers

## Challenge B: Background Job Processing

Add a job queue for asynchronous processing:
* Move email sending to a Spring @Async method backed by a ThreadPoolTaskExecutor
* Add a @Scheduled daily digest job that collects low-stock products and emails admins
* Add PDF report generation as an asynchronous job: endpoint returns a job ID immediately, a second endpoint allows polling for the result
* Test async methods in isolation with a synchronous task executor in the test profile

## Challenge C: OpenAPI Documentation

Add interactive API documentation using SpringDoc OpenAPI:
* Integrate springdoc-openapi-starter-webmvc-ui to auto-generate interactive Swagger UI documentation
* Add @Operation, @ApiResponse, and @Schema annotations to every controller and DTO covering request parameters, request body schemas, response shapes, and status codes
* Document authentication requirements (Bearer token) so Swagger UI allows testing protected endpoints directly using the Authorize button
* Include example request/response payloads for each endpoint using @ExampleObject
* Ensure the Swagger UI is accessible at /swagger-ui.html and stays synchronized as routes are added or modified

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 21

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

# Deliverables & Submission Checklist

Upon completion, you must submit the following deliverables for review. Each item will be verified before the exercise is considered complete.

## 1. Source Code Repository

*   A single Git repository (GitHub / GitLab) with the complete project source code
*   Clean commit history showing incremental progress across all 8 phases (avoid single giant commits)
*   A well-written README.md that includes: project description, prerequisites (Java version, Docker for Testcontainers), setup instructions, how to run the application, and how to run the tests
*   An application-example.yml file listing all required configuration properties (without actual secrets)
*   No build artifacts or .env-equivalent files committed; a proper .gitignore is in place

## 2. Working Application

*   The application must start without errors using a single command (e.g., ./mvnw spring-boot:run or ./gradlew bootRun) with a local PostgreSQL instance or Docker Compose file provided
*   All API endpoints must be accessible and respond correctly (demonstrate with Postman, Insomnia, or similar)
*   A Postman collection or OpenAPI spec exported and included in the repository, covering all endpoints with example requests and responses
*   Flyway migrations apply automatically on startup; database schema is reproducible from scratch
*   WebSocket connections can be established from a test client and STOMP messages are received

## 3. Test Suite

*   All tests pass when running ./mvnw test (or ./gradlew test) with zero failures
*   JaCoCo coverage report generated (./mvnw verify) showing 80%+ for lines, branches, methods, and instructions
*   Coverage HTML report accessible under target/site/jacoco/index.html
*   Minimum 80 total tests across all phases
*   Both unit tests (with Mockito mocks/stubs) and integration tests (with MockMvc + Testcontainers) are present
*   No tests that are @Disabled, commented out, or trivially passing (e.g., assertTrue(true))

## 4. Phase-wise Demo

Prepare a short walkthrough (live demo or screen recording of 10-15 minutes) covering:

*   **Phase 1:** Initial project setup, API design, and basic functionality implementation
*   **Phase 2:** Database schema creation, entity mapping, and basic CRUD operations
*   **Phase 3:** Authentication and authorization implementation
*   **Phase 4:** Search functionality and filtering
*   **Phase 5:** Cart management and item addition/removal
*   **Phase 6:** Payment processing integration
*   **Phase 7:** User management and profile editing
*   **Phase 8:** Performance optimization and scalability considerations

## 5. Documentation

*   A comprehensive Javadoc-style API documentation for all public-facing APIs
*   A detailed architecture diagram explaining the system components and their interactions
*   A security policy document outlining authentication, authorization, and data protection strategies
*   A deployment strategy document describing how to deploy the application to a production environment
*   A monitoring and logging strategy document detailing how to set up monitoring and logging for the application

## 6. Additional Requirements

*   All code is written in Java 17+
*   Docker Compose file is provided for easy local development
*   Application is fully containerized using Docker
*   CI/CD pipeline is set up (GitHub Actions or equivalent)
*   Linting is configured and passes
*   Code quality metrics are tracked (e.g., SonarQube)
*   Security best practices are followed throughout the codebase
*   Code is modular, well-organized, and follows SOLID principles
*   Code reviews have been conducted and feedback has been addressed

## 7. Additional Notes

*   The project must be fully functional and ready to be deployed to production.
*   Any external dependencies used must be properly managed and documented.
*   The project must be scalable and able to handle increased traffic.
*   The project must be maintainable and easily extendable for future features.

&lt;watermark&gt;Confidential – Internal Use Only&lt;/watermark&gt;
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 22

StoreFlow API – Comprehensive Exercise
Grootan Technologies Internal Training

21. User signup and login flow – show the JWT being returned and used as a Bearer token for subsequent requests
22. Create a category, create products in that category, and demonstrate search, filtering, and pagination
23. Place an order – show stock being deducted and the order confirmation email being captured by the mock transport
24. Demonstrate validation by sending invalid data and showing the structured error responses
25. Upload a product image and download it back; generate an order PDF and show the output
26. Show a WebSocket client receiving a real-time STOMP notification when an order status is updated
27. Run the full test suite and show the JaCoCo coverage summary

&lt;img&gt;Wrench icon&lt;/img&gt; **Validation Checklist (Reviewer Use)**
1) git clone + mvn install runs cleanly 2) mvn spring-boot:run starts the server against a local PostgreSQL 3) mvn test passes with 0 failures 4) mvn verify shows JaCoCo ≥80% across all metrics 5) Postman collection covers all endpoints 6) Demo covers all 7 walkthrough items

# Evaluation Rubric

<table>
<thead>
<tr>
<th>Criteria</th>
<th>Weight</th>
<th>What We Look For</th>
</tr>
</thead>
<tbody>
<tr>
<td>Test Quality</td>
<td>30%</td>
<td>Meaningful assertions, edge cases, proper Mockito mocking/stubbing, no false positives</td>
</tr>
<tr>
<td>Code Architecture</td>
<td>25%</td>
<td>Clean layered architecture, proper use of Spring stereotypes, DTO separation, DRY principles</td>
</tr>
<tr>
<td>API Design</td>
<td>20%</td>
<td>RESTful conventions, consistent response envelopes, correct HTTP status codes, pagination</td>
</tr>
<tr>
<td>Feature Completeness</td>
<td>15%</td>
<td>All 8 phases implemented and working end-to-end with Flyway migrations applied cleanly</td>
</tr>
<tr>
<td>Error Handling</td>
<td>10%</td>
<td>Graceful errors via @ControllerAdvice, no unhandled exceptions, structured logging</td>
</tr>
</tbody>
</table>

&lt;img&gt;Bulb icon&lt;/img&gt; **Tips for Success**
Write tests first (TDD) whenever possible – it forces you to think about the API contract before implementation. Keep commits small and frequent. Complete each phase fully before moving to the next. When stuck, refer back to the relevant section of the course. Use @DataJpaTest for repository tests and @WebMvcTest for controller tests to keep your test suite fast.

Confidential – Internal Use Only
&lt;page_number&gt;Page&lt;/page_number&gt;

---


## Page 23

StoreFlow API – Comprehensive Exercise Grootan Technologies Internal Training

Confidential – Internal Use Only &lt;page_number&gt;Page&lt;/page_number&gt;