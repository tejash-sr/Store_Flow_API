# StoreFlow API — Implementation Plan

> **Version:** 1.0  
> **Repository:** https://github.com/tejash-sr/StoreFlowAPI  
> **Organization:** Grootan Technologies – Internal Training Program  
> **Last Updated:** 2026-03-31

---

## Table of Contents

1. [Project Structure](#1-project-structure)
2. [Development Environment Setup](#2-development-environment-setup)
3. [Phase-by-Phase Implementation Guide](#3-phase-by-phase-implementation-guide)
4. [Middleware Implementation](#4-middleware-implementation)
5. [Key Implementation Patterns](#5-key-implementation-patterns)
6. [Database Schema & Flyway Migrations](#6-database-schema--flyway-migrations)
7. [Testing Strategy](#7-testing-strategy)
8. [Security Implementation](#8-security-implementation)
9. [Configuration Management](#9-configuration-management)
10. [CI/CD Pipeline](#10-cicd-pipeline)
11. [Dependency Reference](#11-dependency-reference)

---

## 1. Project Structure

```
storeflow-api/
├── .github/workflows/ci.yml
├── docs/
├── postman/
├── src/
│   ├── main/
│   │   ├── java/com/grootan/storeflow/
│   │   │   ├── StoreFlowApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   ├── MailConfig.java
│   │   │   │   ├── ActuatorSecurityConfig.java
│   │   │   │   └── AppConfig.java
│   │   │   ├── middleware/                        # ← ALL FILTERS HERE
│   │   │   │   ├── JwtAuthenticationFilter.java   # Validates Bearer tokens
│   │   │   │   ├── RequestLoggingFilter.java      # MDC trace ID
│   │   │   │   └── RateLimitingFilter.java        # Bucket4j rate limiting
│   │   │   ├── controllers/
│   │   │   │   ├── HealthController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── AdminController.java
│   │   │   ├── services/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── FileStorageService.java
│   │   │   │   ├── PdfGenerationService.java
│   │   │   │   ├── CsvExportService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── NotificationService.java       # WebSocket STOMP publisher
│   │   │   │   └── ScheduledJobService.java       # Daily digest @Scheduled
│   │   │   ├── repositories/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── OrderItemRepository.java
│   │   │   ├── entities/
│   │   │   │   ├── User.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── ShippingAddress.java           # @Embeddable
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── SignupRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── CreateProductRequest.java
│   │   │   │   │   ├── UpdateProductRequest.java
│   │   │   │   │   ├── StockAdjustmentRequest.java
│   │   │   │   │   ├── PlaceOrderRequest.java
│   │   │   │   │   ├── OrderStatusUpdateRequest.java
│   │   │   │   │   └── ForgotPasswordRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── AuthResponse.java
│   │   │   │       ├── UserResponse.java
│   │   │   │       ├── ProductResponse.java
│   │   │   │       ├── OrderResponse.java
│   │   │   │       ├── PageResponse.java
│   │   │   │       ├── CursorPageResponse.java
│   │   │   │       └── ErrorResponse.java
│   │   │   ├── exceptions/
│   │   │   │   ├── AppException.java              # Base exception
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── InsufficientStockException.java
│   │   │   │   ├── InvalidStatusTransitionException.java
│   │   │   │   ├── AuthenticationFailedException.java
│   │   │   │   └── GlobalExceptionHandler.java    # @ControllerAdvice
│   │   │   ├── filters/
│   │   │   │   ├── JwtAuthenticationFilter.java   # OncePerRequestFilter
│   │   │   │   └── RequestLoggingFilter.java      # MDC trace ID
│   │   │   ├── validation/
│   │   │   │   ├── ExistsInDatabase.java          # Custom annotation
│   │   │   │   ├── ExistsInDatabaseValidator.java # ConstraintValidator impl
│   │   │   │   ├── ValidSku.java                  # SKU pattern annotation
│   │   │   │   └── ValidSkuValidator.java
│   │   │   ├── enums/
│   │   │   │   ├── Role.java                      # USER, ADMIN
│   │   │   │   ├── ProductStatus.java             # ACTIVE, INACTIVE, DISCONTINUED
│   │   │   │   └── OrderStatus.java               # PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
│   │   │   ├── utils/
│   │   │   │   ├── JwtUtil.java                   # JWT generation/validation
│   │   │   │   ├── PaginationUtil.java            # Offset + cursor helpers
│   │   │   │   └── OrderStatusTransition.java     # Valid transition graph
│   │   │   └── metrics/
│   │   │       └── OrderMetrics.java              # Micrometer counters/gauges
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/migration/
│   │       │   ├── V1__create_users.sql
│   │       │   ├── V2__create_categories.sql
│   │       │   ├── V3__create_products.sql
│   │       │   └── V4__create_orders.sql
│   │       └── templates/email/
│   │           ├── welcome.html
│   │           ├── password-reset.html
│   │           ├── order-confirmation.html
│   │           ├── low-stock-alert.html
│   │           └── daily-digest.html
│   └── test/
│       ├── java/com/grootan/storeflow/
│       │   ├── AbstractIntegrationTest.java       # Base class with @SpringBootTest + @Testcontainers
│       │   ├── controllers/
│       │   │   ├── HealthControllerTest.java
│       │   │   ├── AuthControllerTest.java
│       │   │   ├── ProductControllerTest.java
│       │   │   └── OrderControllerTest.java
│       │   ├── services/
│       │   │   ├── AuthServiceTest.java
│       │   │   ├── ProductServiceTest.java
│       │   │   ├── OrderServiceTest.java
│       │   │   ├── EmailServiceTest.java
│       │   │   ├── PdfGenerationServiceTest.java
│       │   │   └── CsvExportServiceTest.java
│       │   ├── repositories/
│       │   │   ├── UserRepositoryTest.java
│       │   │   ├── ProductRepositoryTest.java
│       │   │   ├── CategoryRepositoryTest.java
│       │   │   └── OrderRepositoryTest.java
│       │   ├── filters/
│       │   │   └── JwtAuthenticationFilterTest.java
│       │   └── validation/
│       │       ├── ExistsInDatabaseValidatorTest.java
│       │       └── ValidSkuValidatorTest.java
│       └── resources/
│           └── application-test.yml
├── docker-compose.yml
├── Dockerfile
├── .github/workflows/ci.yml
├── pom.xml (or build.gradle)
├── README.md
├── application-example.yml
└── .gitignore
```

---

## 2. Development Environment Setup

### Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | 21 | Runtime |
| Maven | 3.9+ OR Gradle 8+ | Build tool |
| Docker Desktop | Latest | Testcontainers + local DB |
| Git | Latest | Version control |
| IntelliJ IDEA / VS Code | Latest | IDE |
| Postman | Latest | API testing |

### Initial Setup Steps

```bash
# 1. Initialize Spring Boot project (via Spring Initializr or CLI)
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.2.0 \
  -d groupId=com.grootan \
  -d artifactId=storeflow-api \
  -d packageName=com.grootan.storeflow \
  -d javaVersion=21 \
  -d dependencies=web,data-jpa,security,postgresql,flyway,validation,lombok,actuator \
  -o storeflow-api.zip

# 2. Start local PostgreSQL via Docker
docker-compose up -d postgres

# 3. Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Run tests
./mvnw test

# 5. Run with coverage
./mvnw verify
```

### docker-compose.yml

> Note: The `version:` top-level key is removed — Docker Compose v2+ ignores it and emits a deprecation warning.

```yaml
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: storeflow
      POSTGRES_USER: storeflow_user
      POSTGRES_PASSWORD: storeflow_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U storeflow_user"]
      interval: 5s
      timeout: 5s
      retries: 5

  app:
    build: .
    depends_on:
      postgres:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/storeflow
      DB_USERNAME: storeflow_user
      DB_PASSWORD: storeflow_pass
      JWT_SECRET: ${JWT_SECRET}

volumes:
  postgres_data:
```

---

## 3. Phase-by-Phase Implementation Guide

---

### Phase 1 – Project Foundation (Est: 2–3 days)

#### Implementation Order
1. Create project structure (packages + empty classes)
2. Configure `application.yml`
3. Write `AbstractIntegrationTest` base class
4. Implement `HealthController`
5. Implement `GlobalExceptionHandler`
6. Configure `SecurityConfig` (open all routes)
7. Configure JaCoCo in `pom.xml`
8. Write all Phase 1 tests first (TDD)

#### application.yml skeleton

```yaml
spring:
  application:
    name: storeflow-api
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/storeflow}
    username: ${DB_USERNAME:storeflow_user}
    password: ${DB_PASSWORD:storeflow_pass}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    baseline-on-migrate: true

server:
  port: 8080
  compression:
    enabled: true
    min-response-size: 1024

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

#### HealthController Pattern

```java
@RestController
@RequestMapping("/api")
public class HealthController {

    private final long startTime = System.currentTimeMillis();

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(HealthResponse.builder()
            .status("UP")
            .timestamp(Instant.now())
            .jvmUptimeMs(System.currentTimeMillis() - startTime)
            .build());
    }
}
```

#### AbstractIntegrationTest Pattern

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("storeflow_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
```

#### JaCoCo Configuration (pom.xml)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit><counter>LINE</counter><value>COVEREDRATIO</value><minimum>0.80</minimum></limit>
                            <limit><counter>BRANCH</counter><value>COVEREDRATIO</value><minimum>0.80</minimum></limit>
                            <limit><counter>METHOD</counter><value>COVEREDRATIO</value><minimum>0.80</minimum></limit>
                            <limit><counter>INSTRUCTION</counter><value>COVEREDRATIO</value><minimum>0.80</minimum></limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

### Phase 2 – Data Models (Est: 3–4 days)

#### Implementation Order
1. Write Flyway migration V1 (users table)
2. Write Flyway migration V2 (categories table)
3. Write Flyway migration V3 (products table)
4. Write Flyway migration V4 (orders + order_items tables)
5. Implement all entities with JPA annotations
6. Implement all repositories with custom query methods
7. Write @DataJpaTest tests first (TDD), using Testcontainers

#### Entity Pattern – Audit Fields

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
```

#### Order Status Transition Graph

```
PENDING -> CONFIRMED
CONFIRMED -> SHIPPED
SHIPPED -> DELIVERED
PENDING -> CANCELLED
CONFIRMED -> CANCELLED
```
Any other transition is invalid and throws `InvalidStatusTransitionException`.

---

### Phase 3 – REST API Endpoints (Est: 4–5 days)

#### Implementation Order
1. Create all DTOs (Request + Response)
2. Implement `ProductService` (all methods)
3. Implement `ProductController` (thin, delegates to service)
4. Implement `OrderService` (all methods)
5. Implement `OrderController`
6. Write service unit tests with Mockito (TDD)
7. Write MockMvc integration tests

#### Service Pattern

```java
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductResponse create(CreateProductRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", req.getCategoryId()));
        
        Product product = Product.builder()
            .name(req.getName().trim())
            .sku(req.getSku().toUpperCase())
            // ... map all fields
            .category(category)
            .status(ProductStatus.ACTIVE)
            .build();

        return ProductResponse.from(productRepository.save(product));
    }

    // ... other methods
}
```

#### Atomic Stock Deduction Pattern

```java
@Transactional
public OrderResponse placeOrder(PlaceOrderRequest req, UUID userId) {
    // Validate all items first (fail-fast, no partial deductions)
    for (OrderItemRequest item : req.getItems()) {
        Product product = productRepository.findById(item.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProductId()));
        if (product.getStockQuantity() < item.getQuantity()) {
            throw new InsufficientStockException(product.getSku(), item.getQuantity(), product.getStockQuantity());
        }
    }
    
    // Deduct stock and build order items
    List<OrderItem> items = req.getItems().stream()
        .map(item -> {
            Product product = productRepository.findById(item.getProductId()).orElseThrow();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
            return buildOrderItem(product, item.getQuantity());
        })
        .toList();

    // Save order
    Order order = Order.builder()
        .customer(userRepository.getReferenceById(userId))
        .orderItems(items)
        .status(OrderStatus.PENDING)
        .referenceNumber(generateReference())
        .shippingAddress(mapAddress(req.getShippingAddress()))
        .totalAmount(calculateTotal(items))
        .build();

    return OrderResponse.from(orderRepository.save(order));
}
```

---

### Phase 4 – Authentication & Authorization (Est: 4–5 days)

#### Implementation Order
1. Add `JwtUtil` (generate + validate JWT)
2. Implement `JwtAuthenticationFilter`
3. Configure `SecurityConfig` with `SecurityFilterChain`
4. Implement `AuthService` (signup, login, refresh, forgotPassword, resetPassword)
5. Implement `AuthController`
6. Configure rate limiting (Bucket4j)
7. Write all auth tests (TDD)
8. Update all Phase 3 integration tests to include Authorization header

#### JWT Filter Pattern

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token); // throws JwtException if invalid/expired

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtil.isValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }
}
```

---

## 4. Middleware Implementation

> Middleware is **not optional**. All 3 filters must exist from Phase 1 and be fully implemented in Phase 4.

### MW-1: RequestLoggingFilter

```java
// com.grootan.storeflow.middleware.RequestLoggingFilter
@Component
@Order(1)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        res.setHeader(TRACE_HEADER, traceId);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(req, res);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} {} -> {} ({}ms)",
                req.getMethod(), req.getRequestURI(),
                res.getStatus(), duration);
            MDC.clear(); // Always clear — prevents trace ID leaking to next request
        }
    }
}
```

**Logback pattern** (add to `logback-spring.xml`):
```xml
<pattern>%d{ISO8601} [%X{traceId:-NO_TRACE}] %-5level %logger{36} - %msg%n</pattern>
```

---

### MW-2: RateLimitingFilter

```java
// com.grootan.storeflow.middleware.RateLimitingFilter
@Component
@Order(2)
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    // One bucket per IP address
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String ip) {
        return ipBuckets.computeIfAbsent(ip, k ->
            Bucket.builder()
                .addLimit(Bandwidth.classic(5,
                    Refill.intervally(5, Duration.ofMinutes(15))))
                .build()
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        // Only rate-limit auth endpoints
        if (!req.getRequestURI().startsWith("/api/auth/")) {
            chain.doFilter(req, res);
            return;
        }

        String clientIp = req.getRemoteAddr();
        Bucket bucket = resolveBucket(clientIp);

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\"," +
                "\"message\":\"Rate limit exceeded. Try again in 15 minutes.\"}"
            );
        }
    }
}
```

---

### MW-3: JwtAuthenticationFilter

```java
// com.grootan.storeflow.middleware.JwtAuthenticationFilter
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res); // No token — let Security handle it
            return;
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    // Enrich MDC with userId for correlated logs
                    MDC.put("userId", username);
                }
            }
            chain.doFilter(req, res);

        } catch (JwtException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            sendUnauthorized(res, ex.getMessage());
        }
    }

    private void sendUnauthorized(HttpServletResponse res, String message)
            throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
            "status", 401,
            "error", "Unauthorized",
            "message", message,
            "timestamp", Instant.now().toString()
        );
        res.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
```

### Registering Filters in SecurityConfig

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RequestLoggingFilter requestLoggingFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health", "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // Filter order: Logging → Rate Limiting → JWT → Spring Security
            .addFilterBefore(requestLoggingFilter, ChannelProcessingFilter.class)
            .addFilterBefore(rateLimitingFilter,
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

### Phase 5 – Validation & Error Handling (Est: 2–3 days)

#### Implementation Order
1. Add validation annotations to all DTOs
2. Implement `@ExistsInDatabase` custom annotation + validator
3. Implement `@ValidSku` custom annotation + validator
4. Implement `AppException` hierarchy
5. Implement `GlobalExceptionHandler` with all handler methods
6. Write all validation + exception tests

#### Custom Validator Pattern

```java
@Component
@RequiredArgsConstructor
public class ExistsInDatabaseValidator implements ConstraintValidator<ExistsInDatabase, UUID> {

    private final ApplicationContext context;
    private Class<? extends JpaRepository<?, UUID>> repositoryClass;

    @Override
    public void initialize(ExistsInDatabase constraintAnnotation) {
        this.repositoryClass = constraintAnnotation.repository();
    }

    @Override
    public boolean isValid(UUID id, ConstraintValidatorContext context) {
        if (id == null) return true; // let @NotNull handle null
        JpaRepository<?, UUID> repo = this.context.getBean(repositoryClass);
        return repo.existsById(id);
    }
}
```

---

### Phase 6 – File Upload & PDF Generation (Est: 3–4 days)

#### Implementation Order
1. Implement `FileStorageService` (save/load files)
2. Add file upload validation (size + MIME type)
3. Implement image download streaming
4. Implement avatar resize with Thumbnailator
5. Implement `PdfGenerationService` with PDFBox
6. Implement `CsvExportService`
7. Add endpoints to controllers
8. Write all file/PDF/CSV tests

#### PDF Generation Pattern

```java
@Service
public class PdfGenerationService {

    public byte[] generateOrderReport(Order order) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                // Write order reference, customer, date, items, total
                stream.beginText();
                stream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                stream.newLineAtOffset(50, 750);
                stream.showText("Order: " + order.getReferenceNumber());
                // ... more content
                stream.endText();
            }
            doc.save(out);
            return out.toByteArray();
        }
    }
}
```

---

### Phase 7 – Advanced Queries & Real-time (Est: 4–5 days)

#### Implementation Order
1. Implement offset-based pagination wrapper (`PageResponse<T>`)
2. Implement cursor-based pagination utility
3. Implement Product Specifications for flexible search
4. Implement low-stock JPQL query
5. Configure WebSocket + STOMP (`WebSocketConfig`)
6. Implement `NotificationService` (STOMP message publisher)
7. Add JWT interceptor for WebSocket handshake
8. Write all pagination + WebSocket tests

#### JPA Specification Pattern

```java
public class ProductSpecification {

    public static Specification<Product> withFilters(
            String name, UUID categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, ProductStatus status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

#### WebSocket Config

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
```

---

### Phase 8 – Email & Production Readiness (Est: 3–4 days)

#### Implementation Order
1. Implement `EmailService` with 5 email templates
2. Set up Greenmail for testing (application-test.yml)
3. Add `RequestLoggingFilter` with MDC trace ID
4. Configure Actuator + Micrometer metrics
5. Implement `ScheduledJobService` for daily digest
6. Add `application-prod.yml` with all prod settings
7. Configure graceful shutdown
8. Write remaining tests until coverage >= 80%

#### Email Service Pattern

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Thymeleaf or simple HTML

    @Async
    public void sendWelcomeEmail(User user, String verificationLink) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to StoreFlow!");
            helper.setText(buildWelcomeHtml(user.getFullName(), verificationLink), true);
            mailSender.send(message);
            log.info("Welcome email sent to {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}", user.getEmail(), e);
        }
    }
    // ... other email methods
}
```

#### MDC Trace ID Filter

```java
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        res.setHeader(TRACE_ID_HEADER, traceId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}
```

---

## 4. Key Implementation Patterns

### Response Envelope
All successful responses use a consistent shape:

```json
{
  "data": { },
  "timestamp": "2026-03-31T12:00:00Z",
  "status": 200
}
```

All error responses:

```json
{
  "timestamp": "2026-03-31T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "errors": {
    "name": "must not be blank",
    "price": "must be positive"
  }
}
```

### Paginated Response

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false,
  "hasNext": true
}
```

### Cursor-based Response

```json
{
  "content": [],
  "nextCursor": "eyJpZCI6IjEyMyJ9",
  "hasMore": true,
  "size": 20
}
```

---

## 5. Database Schema & Flyway Migrations

### V1__create_users.sql

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    avatar_path VARCHAR(500),
    reset_token VARCHAR(255),
    reset_token_expires_at TIMESTAMPTZ,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
```

### V2__create_categories.sql

```sql
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### V3__create_products.sql

```sql
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    price NUMERIC(10,2) NOT NULL CHECK (price > 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    category_id UUID NOT NULL REFERENCES categories(id),
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
```

### V4__create_orders.sql

```sql
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipping_street VARCHAR(255) NOT NULL,
    shipping_city VARCHAR(100) NOT NULL,
    shipping_country VARCHAR(100) NOT NULL,
    shipping_postal_code VARCHAR(20) NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10,2) NOT NULL CHECK (unit_price > 0),
    subtotal NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
```

---

## 6. Testing Strategy

### Test Layers

| Layer | Annotation | DB | Speed |
|-------|-----------|-----|-------|
| Unit | `@ExtendWith(MockitoExtension.class)` | None | Fast |
| Repository | `@DataJpaTest` + Testcontainers | Real PostgreSQL | Medium |
| Controller Slice | `@WebMvcTest` | Mocked service | Fast |
| Integration | `@SpringBootTest` + Testcontainers | Real PostgreSQL | Slow |

### Mockito Best Practices

```java
// Stub return values
when(productRepository.findById(id)).thenReturn(Optional.of(product));

// Verify interactions
verify(emailService, times(1)).sendOrderConfirmation(any(Order.class));

// Argument Captors
ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
verify(productRepository).save(productCaptor.capture());
assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.DISCONTINUED);

// Spy for partial mocking
AuthService spyService = spy(authService);
doReturn("mocked-token").when(spyService).generateToken(any());
```

### Test Naming Convention
```
methodName_stateUnderTest_expectedBehavior
// Examples:
placeOrder_insufficientStock_throwsInsufficientStockException
signup_validRequest_returnsJwtToken
getProducts_withCategoryFilter_returnsFilteredResults
```

---

## 7. Security Implementation

### SecurityFilterChain Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfig()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/health", "/actuator/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

## 8. Configuration Management

### application-dev.yml

```yaml
spring:
  jpa:
    show-sql: true
  mail:
    host: localhost
    port: 3025 # Greenmail SMTP port for dev

logging:
  level:
    com.grootan.storeflow: DEBUG
    org.springframework.security: DEBUG
```

### application-test.yml

> **Fix:** Use `validate` (not `create-drop`) so Flyway fully owns the schema in tests.
> `create-drop` conflicts with Flyway — Hibernate drops tables on shutdown and corrupts `flyway_schema_history`.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate          # Flyway owns the schema — never use create-drop with Flyway
  flyway:
    enabled: true
    baseline-on-migrate: true     # Required so Flyway runs correctly in test context
  mail:
    host: localhost
    port: 3025                    # Greenmail embedded SMTP

logging:
  level:
    root: WARN
    com.grootan.storeflow: INFO
```

### application-prod.yml

```yaml
spring:
  jpa:
    show-sql: false

server:
  shutdown: graceful
  compression:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      show-details: never

logging:
  level:
    root: WARN
    com.grootan.storeflow: INFO
  pattern:
    console: "%d{ISO8601} [%X{traceId}] %-5level %logger{36} - %msg%n"
```

---

## 9. CI/CD Pipeline

### .github/workflows/ci.yml

```yaml
name: StoreFlow API CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: storeflow_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Run Tests with Coverage
        # ./mvnw verify already enforces JaCoCo 80% gate — no second step needed
        run: ./mvnw verify -Dspring.profiles.active=test
      
      - name: Upload JaCoCo Report
        if: always()              # Upload even if tests fail so you can inspect coverage
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: target/site/jacoco/
```

---

## 10. Dependency Reference

### pom.xml Core Dependencies

```xml
<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<!-- Required for Flyway 10+ with PostgreSQL — without this you get ClassNotFoundException at startup -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- PDF Generation -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>

<!-- Image Resizing -->
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
    <version>0.4.20</version>
</dependency>

<!-- Rate Limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.icegreen</groupId>
    <artifactId>greenmail-junit5</artifactId>
    <version>2.0.0</version>
    <scope>test</scope>
</dependency>
```
