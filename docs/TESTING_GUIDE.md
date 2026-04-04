# StoreFlow API — Testing Guide

> **Version:** 1.0  
> **Repository:** https://github.com/tejash-sr/StoreFlowAPI  
> **Organization:** Grootan Technologies – Internal Training Program  
> **Last Updated:** 2026-03-31

---

## Table of Contents

1. [Testing Philosophy](#1-testing-philosophy)
2. [Test Infrastructure Setup](#2-test-infrastructure-setup)
3. [Unit Testing Guide](#3-unit-testing-guide)
4. [Repository Testing Guide](#4-repository-testing-guide)
5. [Controller / Integration Testing Guide](#5-controller--integration-testing-guide)
6. [Security Testing Guide](#6-security-testing-guide)
7. [Phase-wise Test Checklist](#7-phase-wise-test-checklist)
8. [Coverage Requirements](#8-coverage-requirements)
9. [Test Naming Conventions](#9-test-naming-conventions)
10. [Common Mockito Patterns](#10-common-mockito-patterns)
11. [Testcontainers Reference](#11-testcontainers-reference)
12. [Troubleshooting](#12-troubleshooting)

---

## 1. Testing Philosophy

### TDD First
Write your test **before** or **alongside** your implementation code. This forces you to think about the API contract and edge cases upfront.

```
Red → Green → Refactor
Write failing test → Make it pass → Clean up code
```

### Test Pyramid

```
                    ┌──────┐
                    │  E2E │  (Demo / Manual)
                   ┌┴──────┴┐
                   │Integr. │  @SpringBootTest + Testcontainers
                  ┌┴────────┴┐
                  │  Slice   │  @WebMvcTest, @DataJpaTest
                 ┌┴──────────┴┐
                 │    Unit    │  @ExtendWith(MockitoExtension.class)
                 └────────────┘
```

### Rules
- **Unit tests** should run in milliseconds — no Spring context, no DB
- **Repository tests** use `@DataJpaTest` + Testcontainers — real DB, no web layer
- **Integration tests** use `@SpringBootTest` + Testcontainers + MockMvc — full stack
- No `@Disabled` tests, no `assertTrue(true)`, no commented-out tests

---

## 2. Test Infrastructure Setup

### AbstractIntegrationTest Base Class

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("storeflow_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Helper: get JWT for test user
    protected String obtainToken(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email, "password", password))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }
}
```

### AbstractRepositoryTest Base Class

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class AbstractRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("storeflow_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

---

## 3. Unit Testing Guide

### When to Write Unit Tests
- All `@Service` methods
- `@ControllerAdvice` handlers
- `JwtAuthenticationFilter`
- Custom `ConstraintValidator` implementations
- Utility classes (`JwtUtil`, `PaginationUtil`, etc.)
- Email service methods

### Template: Service Unit Test

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    // Test data
    private Category sampleCategory;
    private CreateProductRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
            .id(UUID.randomUUID())
            .name("Electronics")
            .build();

        validRequest = CreateProductRequest.builder()
            .name("Test Product")
            .description("A detailed product description here")
            .sku("ELEC-001")
            .price(new BigDecimal("299.99"))
            .stockQuantity(50)
            .categoryId(sampleCategory.getId())
            .build();
    }

    @Test
    @DisplayName("create() should save product and return response when category exists")
    void create_validRequest_returnsProductResponse() {
        // ARRANGE
        when(categoryRepository.findById(validRequest.getCategoryId()))
            .thenReturn(Optional.of(sampleCategory));
        when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });

        // ACT
        ProductResponse response = productService.create(validRequest);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getSku()).isEqualTo("ELEC-001");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("create() should throw ResourceNotFoundException when category not found")
    void create_categoryNotFound_throwsResourceNotFoundException() {
        // ARRANGE
        when(categoryRepository.findById(any(UUID.class)))
            .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> productService.create(validRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Category");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete() should set status to DISCONTINUED, not remove row")
    void delete_existingProduct_setsStatusToDiscontinued() {
        // ARRANGE
        Product product = Product.builder()
            .id(UUID.randomUUID())
            .status(ProductStatus.ACTIVE)
            .build();
        when(productRepository.findById(product.getId()))
            .thenReturn(Optional.of(product));

        // ACT
        productService.delete(product.getId());

        // ASSERT
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }
}
```

### Template: OrderService Stock Tests

```java
@Test
@DisplayName("placeOrder() should throw InsufficientStockException when stock is too low")
void placeOrder_insufficientStock_throwsInsufficientStockException() {
    // ARRANGE
    Product lowStockProduct = Product.builder()
        .id(UUID.randomUUID())
        .sku("PROD-001")
        .stockQuantity(2)
        .build();
    PlaceOrderRequest request = /* build request with quantity=5 for lowStockProduct */;

    when(productRepository.findById(lowStockProduct.getId()))
        .thenReturn(Optional.of(lowStockProduct));

    // ACT + ASSERT
    assertThatThrownBy(() -> orderService.placeOrder(request, UUID.randomUUID()))
        .isInstanceOf(InsufficientStockException.class);

    verify(orderRepository, never()).save(any());
}

@Test
@DisplayName("placeOrder() should correctly deduct stock for each OrderItem")
void placeOrder_validRequest_deductsStockForEachItem() {
    // ARRANGE
    Product product = Product.builder()
        .id(UUID.randomUUID())
        .sku("PROD-001")
        .stockQuantity(10)
        .price(new BigDecimal("50.00"))
        .build();
    // ... setup request with quantity=3

    when(productRepository.findById(product.getId()))
        .thenReturn(Optional.of(product));
    when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    // ACT
    orderService.placeOrder(request, userId);

    // ASSERT
    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository).save(productCaptor.capture());
    assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(7); // 10-3
}
```

---

## 4. Repository Testing Guide

### When to Use @DataJpaTest
- All custom repository query methods
- Unique constraint validation
- Cascade operations
- Self-referencing relationships

### Template: Product Repository Test

```java
@ExtendWith(SpringExtension.class)
class ProductRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category category;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(Category.builder()
            .name("Electronics")
            .status(CategoryStatus.ACTIVE)
            .build());
    }

    @Test
    @DisplayName("findAllByStockQuantityLessThan() returns only low-stock products")
    void findAllByStockQuantityLessThan_returnsLowStockOnly() {
        // ARRANGE
        productRepository.save(buildProduct("PROD-001", 5));
        productRepository.save(buildProduct("PROD-002", 15));
        productRepository.save(buildProduct("PROD-003", 3));
        entityManager.flush();
        entityManager.clear();

        // ACT
        List<Product> lowStock = productRepository.findAllByStockQuantityLessThan(10);

        // ASSERT
        assertThat(lowStock).hasSize(2);
        assertThat(lowStock).extracting(Product::getSku)
            .containsExactlyInAnyOrder("PROD-001", "PROD-003");
    }

    @Test
    @DisplayName("SKU uniqueness constraint prevents duplicates at database level")
    void save_duplicateSku_throwsDataIntegrityViolationException() {
        // ARRANGE
        productRepository.save(buildProduct("ELEC-001", 10));
        entityManager.flush();

        // ACT + ASSERT
        assertThatThrownBy(() -> {
            productRepository.save(buildProduct("ELEC-001", 20));
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Product buildProduct(String sku, int stock) {
        return Product.builder()
            .name("Product " + sku)
            .description("Description for " + sku)
            .sku(sku)
            .price(new BigDecimal("99.99"))
            .stockQuantity(stock)
            .category(category)
            .status(ProductStatus.ACTIVE)
            .build();
    }
}
```

---

## 5. Controller / Integration Testing Guide

### Template: Integration Test with MockMvc

```java
class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create admin user + get token
        // (or pre-seed via SQL)
        adminToken = obtainToken("admin@storeflow.com", "Admin123!");
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/products with valid payload returns 201 Created")
    void createProduct_validRequest_returns201() throws Exception {
        // ARRANGE
        UUID categoryId = createCategory("Electronics");
        CreateProductRequest req = CreateProductRequest.builder()
            .name("MacBook Pro")
            .description("Apple laptop with M3 chip, great for development")
            .sku("APPLE-MBP-M3")
            .price(new BigDecimal("2499.00"))
            .stockQuantity(10)
            .categoryId(categoryId)
            .build();

        // ACT + ASSERT
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("MacBook Pro"))
            .andExpect(jsonPath("$.sku").value("APPLE-MBP-M3"))
            .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/products with missing required fields returns 400 with field errors")
    void createProduct_missingFields_returns400WithFieldErrors() throws Exception {
        // ARRANGE - empty request body
        CreateProductRequest req = CreateProductRequest.builder()
            .name("") // blank name
            .build();

        // ACT + ASSERT
        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").existsFor("name"))
            .andExpect(jsonPath("$.errors.price").exists())
            .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    @DisplayName("GET /api/products returns paginated list with correct metadata")
    void getProducts_returnsPaginatedList() throws Exception {
        // ARRANGE - create 25 products
        UUID catId = createCategory("Test Category");
        for (int i = 0; i < 25; i++) {
            createProduct("PROD-" + String.format("%03d", i), catId);
        }

        // ACT + ASSERT
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(20))
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("POST /api/orders fails with 409 when stock is insufficient")
    void placeOrder_insufficientStock_returns409() throws Exception {
        // ARRANGE
        UUID catId = createCategory("Cat");
        UUID productId = createProductWithStock("PROD-001", catId, 2); // only 2 in stock
        String userToken = obtainToken("user@storeflow.com", "User123!");

        PlaceOrderRequest req = PlaceOrderRequest.builder()
            .items(List.of(PlaceOrderRequest.OrderItemRequest.builder()
                .productId(productId)
                .quantity(5) // more than available
                .build()))
            .shippingAddress(/* valid address */)
            .build();

        // ACT + ASSERT
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }
}
```

---

## 6. Security Testing Guide

### Template: JWT Filter Unit Test

```java
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Test
    @DisplayName("doFilterInternal() passes request when no Authorization header present")
    void doFilter_noAuthHeader_proceedsWithoutAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal() returns 401 for expired JWT")
    void doFilter_expiredJwt_returnsUnauthorized() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer expired.token.here");
        when(jwtUtil.extractUsername("expired.token.here"))
            .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }
}
```

### Template: Auth Integration Tests

```java
@Test
@DisplayName("Full signup → use JWT → GET /api/auth/me flow")
void signup_thenGetMe_returnsUserProfile() throws Exception {
    // STEP 1: Signup
    String signupBody = mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"new@user.com","password":"Password1!","fullName":"New User"}
            """))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    String jwt = objectMapper.readTree(signupBody).get("accessToken").asText();

    // STEP 2: Use JWT to access protected endpoint
    mockMvc.perform(get("/api/auth/me")
            .header("Authorization", "Bearer " + jwt))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("new@user.com"))
        .andExpect(jsonPath("$.fullName").value("New User"));
}

@Test
@DisplayName("Accessing ADMIN endpoint as USER role returns 403")
void accessAdminEndpoint_asUser_returns403() throws Exception {
    String userToken = obtainToken("user@storeflow.com", "User123!");

    mockMvc.perform(get("/api/admin/products/low-stock")
            .header("Authorization", "Bearer " + userToken))
        .andExpect(status().isForbidden());
}

@Test
@DisplayName("Accessing protected endpoint without token returns 401")
void accessProtectedEndpoint_noToken_returns401() throws Exception {
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isUnauthorized());
}
```

---

## 7. Phase-wise Test Checklist

### Phase 1 — Minimum 6 Tests

| # | Test | Type | Status |
|---|------|------|--------|
| 1.1 | GlobalExceptionHandler returns HTTP 500 for RuntimeException | Unit | ☐ |
| 1.2 | GlobalExceptionHandler returns correct status for domain exceptions | Unit | ☐ |
| 1.3 | Unknown errors serialized with correct JSON shape | Unit | ☐ |
| 1.4 | GET /api/health returns 200 with status, timestamp, uptime | Integration | ☐ |
| 1.5 | GET /api/nonexistent returns 404 with error message | Integration | ☐ |
| 1.6 | CORS headers present when Origin header sent | Integration | ☐ |
| 1.7 | GET /actuator/health returns UP with DB connectivity | Integration | ☐ |
| **MW** | **requestLoggingFilter adds X-Trace-Id header to every response** | **Unit** | **☐** |
| **MW** | **requestLoggingFilter clears MDC after request completes** | **Unit** | **☐** |

### Phase 2 — Minimum 15 Tests

| # | Test | Type | Status |
|---|------|------|--------|
| 2.1 | User email uniqueness enforced at DB level | Repository | ☐ |
| 2.2 | User validation passes for valid email, name, password | Repository | ☐ |
| 2.3 | User validation fails for invalid email format | Unit | ☐ |
| 2.4 | Product SKU uniqueness enforced | Repository | ☐ |
| 2.5 | Product price rejects negative values | Repository | ☐ |
| 2.6 | findAllByStockQuantityLessThan returns only matching products | Repository | ☐ |
| 2.7 | Order cascades save to OrderItems | Repository | ☐ |
| 2.8 | Order total consistent with sum of OrderItem subtotals | Repository | ☐ |
| 2.9 | Category self-referencing parent/child persisted correctly | Repository | ☐ |
| 2.10 | Flyway migrations apply in correct order on fresh schema | Integration | ☐ |
| 2.11 | isResetTokenValid() returns false for expired token | Unit | ☐ |
| 2.12 | isResetTokenValid() returns false for null token | Unit | ☐ |
| 2.13 | findBySkuIgnoreCase finds product regardless of case | Repository | ☐ |
| 2.14 | findByCategory returns only products for that category | Repository | ☐ |
| 2.15 | stockQuantity rejects negative values (DB constraint) | Repository | ☐ |

### Phase 3 — Minimum 20 Tests

| # | Test | Type | Status |
|---|------|------|--------|
| 3.1 | ProductService.create flows data correctly to repository | Unit | ☐ |
| 3.2 | ProductService.create throws when category not found | Unit | ☐ |
| 3.3 | OrderService.placeOrder throws InsufficientStockException | Unit | ☐ |
| 3.4 | OrderService.placeOrder correctly deducts stock | Unit | ☐ |
| 3.5 | OrderService.updateStatus throws for invalid transitions | Unit | ☐ |
| 3.6 | ProductService.delete sets DISCONTINUED (no DB delete) | Unit | ☐ |
| 3.7 | POST /api/products valid data → 201 + created product | Integration | ☐ |
| 3.8 | POST /api/products missing fields → 400 + error details | Integration | ☐ |
| 3.9 | GET /api/products correct pagination metadata | Integration | ☐ |
| 3.10 | GET /api/products?category=X filters correctly | Integration | ☐ |
| 3.11 | GET /api/products/{id} returns product with category | Integration | ☐ |
| 3.12 | GET /api/products/{id} non-existent → 404 | Integration | ☐ |
| 3.13 | PUT /api/products/{id} updates all fields | Integration | ☐ |
| 3.14 | PATCH /api/products/{id}/stock adjusts quantity | Integration | ☐ |
| 3.15 | PATCH /api/products/{id}/stock rejects negative result | Integration | ☐ |
| 3.16 | POST /api/orders → 201 with order reference | Integration | ☐ |
| 3.17 | POST /api/orders → stock deducted correctly | Integration | ☐ |
| 3.18 | POST /api/orders insufficient stock → 409 | Integration | ☐ |
| 3.19 | GET /api/orders USER sees only own orders | Integration | ☐ |
| 3.20 | GET /api/orders ADMIN sees all orders | Integration | ☐ |

### Phase 4 — Minimum 14 Tests

| # | Test | Type | Status |
|---|------|------|--------|
| 4.1 | JwtFilter passes request with valid JWT | Unit | ☐ |
| 4.2 | JwtFilter returns 401 for missing token | Unit | ☐ |
| 4.3 | JwtFilter returns 401 for expired token | Unit | ☐ |
| 4.4 | JwtFilter returns 401 for malformed token | Unit | ☐ |
| 4.5 | AuthService.signup hashes password with BCrypt | Unit | ☐ |
| 4.6 | AuthService.login throws for wrong email | Unit | ☐ |
| 4.7 | AuthService.login throws for wrong password | Unit | ☐ |
| 4.8 | AuthService.resetPassword throws for expired token | Unit | ☐ |
| 4.9 | AuthService.forgotPassword calls emailService once | Unit | ☐ |
| **MW** | **rateLimitingFilter returns 429 after 5 requests from same IP** | **Unit** | **☐** |
| **MW** | **rateLimitingFilter bypasses non-auth routes** | **Unit** | **☐** |
| **MW** | **jwtFilter returns 401 for malformed JWT string** | **Unit** | **☐** |
| **MW** | **jwtFilter returns 401 for wrong-signature JWT** | **Unit** | **☐** |
| **MW** | **6th consecutive auth request from same IP returns 429** | **Integration** | **☐** |
| 4.10 | Full signup → JWT → GET /api/auth/me flow | Integration | ☐ |
| 4.11 | Login with correct credentials → access + refresh tokens | Integration | ☐ |
| 4.12 | Login with wrong password → 401 | Integration | ☐ |
| 4.13 | Protected endpoint without token → 401 | Integration | ☐ |
| 4.14 | ADMIN endpoint as USER role → 403 | Integration | ☐ |

### Phase 5 — Minimum 12 Tests

| # | Test | Type | Status |
|---|------|------|--------|
| 5.1 | @ExistsInDatabase validator with valid ID → passes | Unit | ☐ |
| 5.2 | @ExistsInDatabase validator with invalid ID → fails | Unit | ☐ |
| 5.3 | SKU pattern validator accepts valid SKUs | Unit | ☐ |
| 5.4 | SKU pattern validator rejects lowercase/special chars | Unit | ☐ |
| 5.5 | AppException subclasses carry correct HttpStatus | Unit | ☐ |
| 5.6 | ControllerAdvice maps MethodArgumentNotValidException to 400 | Unit | ☐ |
| 5.7 | ControllerAdvice maps DataIntegrityViolationException to 409 | Unit | ☐ |
| 5.8 | ControllerAdvice maps JwtException to 401 | Unit | ☐ |
| 5.9 | POST with invalid product name field → 400 with field error | Integration | ☐ |
| 5.10 | POST with invalid price → 400 with field error | Integration | ☐ |
| 5.11 | Duplicate SKU on product creation → 409 with field name | Integration | ☐ |
| 5.12 | All error HTTP status codes return correct shape | Integration | ☐ |

### Phase 6 — Minimum 8 Tests

| # | Test | Type | Status |
|---|------|------|--------|
| 6.1 | File upload rejects files over 5MB → 400 | Unit | ☐ |
| 6.2 | File upload rejects disallowed MIME types → 400 | Unit | ☐ |
| 6.3 | PDF generation produces non-empty byte array | Unit | ☐ |
| 6.4 | CSV generation produces correct header + data rows | Unit | ☐ |
| 6.5 | Upload image then download → body and Content-Type match | Integration | ☐ |
| 6.6 | Oversized file upload returns 400 with error message | Integration | ☐ |
| 6.7 | GET /api/orders/{id}/report returns Content-Type application/pdf | Integration | ☐ |
| 6.8 | Upload avatar → GET /api/auth/me returns non-null avatarUrl | Integration | ☐ |

### Phase 7 — Minimum 10 Tests

| # | Test | Type | Status |
|---|------|------|--------|
| 7.1 | Cursor pagination returns correct nextCursor and hasMore | Unit | ☐ |
| 7.2 | Offset pagination calculates totalPages correctly | Unit | ☐ |
| 7.3 | NotificationService invoked with correct topic on status change | Unit | ☐ |
| 7.4 | WebSocket interceptor rejects connections without JWT | Unit | ☐ |
| 7.5 | Product search with combined filters returns correct results | Integration | ☐ |
| 7.6 | Pagination with various page/size returns correct slices | Integration | ☐ |
| 7.7 | Cursor pagination traverses full dataset without gaps | Integration | ☐ |
| 7.8 | Low-stock report returns only products below threshold | Integration | ☐ |
| 7.9 | WebSocket client receives notification after status update | Integration | ☐ |
| 7.10 | Empty result set handled gracefully (content=[], hasNext=false) | Integration | ☐ |

### Phase 8 — Minimum 7 Tests

| # | Test | Type | Status |
|---|------|------|--------|
| 8.1 | Email service composes correct HTML for each of 5 types | Unit | ☐ |
| 8.2 | Email service uses mock transport (no real SMTP) | Unit | ☐ |
| 8.3 | Low-stock alert identifies correct products after deduction | Unit | ☐ |
| 8.4 | Full user journey: signup → login → create product → order → status → email | Integration | ☐ |
| 8.5 | GET /actuator/health returns UP with DB confirmed | Integration | ☐ |
| 8.6 | Custom Micrometer counter increments after order placement | Integration | ☐ |
| 8.7 | JaCoCo report shows >= 80% coverage for all metrics | Coverage | ☐ |

---

## 8. Coverage Requirements

```
Minimum 80% for ALL four metrics:
  - LINE      coverage >= 80%
  - BRANCH    coverage >= 80%
  - METHOD    coverage >= 80%
  - INSTRUCTION coverage >= 80%
```

Enforced at: `./mvnw verify`

JaCoCo HTML report: `target/site/jacoco/index.html`

### Exclusions (add to JaCoCo config if needed)

```xml
<excludes>
    <exclude>**/StoreFlowApplication.class</exclude>
    <exclude>**/dto/**</exclude>
    <exclude>**/entities/**</exclude>
    <exclude>**/enums/**</exclude>
</excludes>
```

---

## 9. Test Naming Conventions

### Method Name Format
```
methodName_stateOrInput_expectedBehavior
```

### Examples
```java
create_validProductRequest_returnsProductResponseWith201()
create_categoryNotFound_throwsResourceNotFoundException()
placeOrder_insufficientStock_throwsInsufficientStockException()
placeOrder_validItems_deductsStockCorrectly()
signup_existingEmail_returns409WithConflictMessage()
login_wrongPassword_returns401()
getProducts_withCategoryFilter_returnsOnlyMatchingProducts()
generatePdf_validOrder_returnsNonEmptyByteArray()
```

---

## 10. Common Mockito Patterns

### Stubbing Return Values
```java
when(productRepository.findById(id)).thenReturn(Optional.of(product));
when(productRepository.findAll(any(Pageable.class))).thenReturn(page);
when(jwtUtil.extractUsername(anyString())).thenReturn("user@test.com");
```

### Stubbing Void Methods
```java
doNothing().when(emailService).sendWelcomeEmail(any());
doThrow(new MessagingException("SMTP down")).when(mailSender).send(any(MimeMessage.class));
```

### Verifying Interactions
```java
verify(productRepository, times(1)).save(any(Product.class));
verify(emailService, never()).sendOrderConfirmation(any());
verify(notificationService).publishOrderStatusChange(eq(orderId), eq(OrderStatus.CONFIRMED));
```

### Argument Captors
```java
ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
verify(productRepository).save(captor.capture());
Product saved = captor.getValue();
assertThat(saved.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
assertThat(saved.getDeletedAt()).isNotNull();
```

### Spying
```java
@Spy
private AuthService authService = new AuthService(/* deps */);

doReturn("fixed-token").when(authService).generateJwt(any());
```

---

## 11. Testcontainers Reference

### Maven Dependency
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-bom</artifactId>
            <version>1.19.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Singleton Container Pattern (for speed)
```java
// In AbstractIntegrationTest — container shared across test classes
static final PostgreSQLContainer<?> postgres;
static {
    postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    postgres.start();
}
```

---

## 12. Troubleshooting

| Problem | Solution |
|---------|---------|
| `Docker not running` Testcontainers error | Start Docker Desktop before running tests |
| Tests pass locally but fail in CI | Ensure Docker is available in CI runner; check Testcontainers version |
| JaCoCo coverage below 80% | Check which packages are excluded; add tests for untested paths |
| `BeanCreationException` in tests | Verify `@ActiveProfiles("test")` is set; check `application-test.yml` |
| Flyway migration fails in tests | Use `spring.flyway.clean-on-validation-error=true` in test profile |
| Port already in use | Use `WebEnvironment.RANDOM_PORT` in `@SpringBootTest` |
| MockMvc returns 403 instead of expected status | Add `@WithMockUser` or include Authorization header in test |
| `NullPointerException` in Mockito test | Ensure `@InjectMocks` and `@Mock` are used together with `MockitoExtension` |
