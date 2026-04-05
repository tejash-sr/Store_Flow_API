# StoreFlow API

A production-ready, enterprise-grade e-commerce API built with Spring Boot 3.2.3, featuring GraphQL, async job processing, comprehensive API documentation, and 250+ passing tests.

**Version:** v0.9.0 | **Build Status:** ✅ All 250 Tests Passing | **Code Coverage:** JaCoCo Enforced

## 🚀 Quick Start

### Prerequisites
- Java 21+
- PostgreSQL 15+
- Maven 3.9+

### Local Development
```bash
# Clone repository
git clone https://github.com/tejash-sr/Store_Flow_API.git
cd Store_Flow_API

# Configure database
export DB_USER=postgres
export DB_PASSWORD=your_password
export DB_NAME=storeflow

# Run application
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run tests
mvn clean test
```

**Access Points:**
- REST API: `http://localhost:8080/api/`
- GraphQL IDE: `http://localhost:8080/graphiql`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- GraphQL Endpoint: `POST http://localhost:8080/graphql`

---

## ✨ Core Features

### REST API (Phases 1-7)
- Complete product, order, user, category management
- Advanced filtering & pagination with cursor-based navigation
- File upload/download with image resizing
- WebSocket real-time notifications
- Comprehensive input validation (JSR-303)

### GraphQL API (Challenge A) ✨
- Full CRUD mutations with role-based access control
- Nested field resolution (products → categories, orders → items)
- JWT authentication with authorization
- Interactive GraphiQL IDE for exploration

### Async Job Processing (Challenge B) ⚡
- ThreadPool-based task execution (email, PDF, generic jobs)
- Job lifecycle tracking: PENDING → PROCESSING → COMPLETED/FAILED
- Progress callbacks and polling support
- Automatic cleanup of old jobs

### API Documentation (Challenge C) 📚
- Interactive Swagger UI with request/response examples
- Bearer token authorization testing
- Complete OpenAPI 3.0 specification (JSON/YAML)
- Ready for client code generation

### Email Notifications (Phase 8)
- Welcome emails, order confirmations, password resets
- Daily digest reports for admins
- HTML-formatted email templates
- Async delivery with retry logic

---

## 🏗 Architecture

```
src/main/java/com/storeflow/storeflow_api/
├── config/              → Spring configs (Security, GraphQL, OpenAPI, Async)
├── controller/          → REST endpoints
├── graphql/             → GraphQL resolvers & schema
├── service/             → Business logic (Email, PDF, Async)
├── repository/          → JPA data access & custom queries
├── entity/              → JPA entities & enums
└── dto/                 → Request/response DTOs

src/main/resources/
├── db/migration/        → Flyway migrations (V1-V9)
├── graphql/schema.graphqls
└── application.yml

src/test/java/          → 250+ unit & integration tests
```

**Key Dependencies:**
- Spring Boot 3.2.3
- Spring Security 6.x (JWT)
- Spring Data JPA
- GraphQL 21.0
- PostgreSQL JDBC
- Flyway (Database versioning)
- JUnit 5, Mockito, Testcontainers
- SpringDoc OpenAPI 2.2.0

---

## 🧪 Testing

```bash
# Run all tests (250+)
mvn clean test

# Run specific test class
mvn test -Dtest=OrderControllerTest

# Run with coverage (JaCoCo)
mvn clean test jacoco:report
# Coverage report: target/site/jacoco/index.html
```

**Test Coverage:**
- 18 controller tests (REST endpoints)
- 15 service tests (business logic)
- 7 GraphQL tests (queries & mutations)
- 6 async job tests (job lifecycle)
- 21 repository tests (data access)
- 80%+ code coverage enforced by JaCoCo

---

## 🔑 Authentication

### JWT Tokens
```bash
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}

# Use in requests
Authorization: Bearer <token>
```

### Default Users
- **admin** / admin123 (ROLE_ADMIN)
- **user** / user123 (ROLE_USER)

---

## 📊 Git Branches & Tags

```
main (v0.9.0) ← Production ready
  ├─ develop
  ├─ challenge-a-graphql
  ├─ challenge-b-async-jobs
  ├─ challenge-c-openapi
  └─ phase-1-foundation → phase-8-email-notifications

Tags: v0.1.0 → v0.9.0 (incremental releases)
```

---

## 🚀 Production Deployment

### Docker (Recommended)
```bash
mvn clean package -DskipTests
docker build -t storeflow-api:0.9.0 .
docker run -e SPRING_PROFILES_ACTIVE=prod \
           -e DB_HOST=postgres \
           -p 8080:8080 \
           storeflow-api:0.9.0
```

### Environment Variables (Production)
```bash
DB_HOST=prod-db.example.com
DB_PORT=5432
DB_NAME=storeflow_prod
DB_USER=storeflow_user
DB_PASSWORD=<secure_password>

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=noreply@yourdomain.com
MAIL_PASSWORD=<app_password>

JWT_SECRET=<base64_encoded_secret>
```

### Database Setup
```sql
CREATE DATABASE storeflow_prod;
CREATE USER storeflow_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE storeflow_prod TO storeflow_user;
```

Flyway migrations run automatically on startup (V1-V9).

---

## 📈 API Examples

### Create Product
```bash
POST /api/products
Authorization: Bearer <token>

{
  "name": "Premium Widget",
  "sku": "PWD-001",
  "price": 99.99,
  "stockQuantity": 50,
  "categoryId": 1
}
```

### Place Order
```bash
POST /api/orders
Authorization: Bearer <token>

{
  "items": [{"productId": 123, "quantity": 2}],
  "shippingAddressId": 1,
  "paymentMethod": "CREDIT_CARD"
}
```

### GraphQL Query
```graphql
POST /api/graphql

{
  orders(first: 5) {
    edges {
      node {
        id
        orderNumber
        total
        customer { name email }
        items { product { name } quantity }
      }
    }
  }
}
```

### Async PDF Generation
```bash
POST /api/jobs/generate-pdf
Authorization: Bearer <token>

{
  "orderId": 789,
  "format": "PDF"
}

# Response
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "progress": 0
}

# Poll for status
GET /api/jobs/550e8400-e29b-41d4-a716-446655440000
```

---

## 🔒 Security Features

- ✅ JWT authentication with HS256
- ✅ Role-based access control (ADMIN, USER, GUEST)
- ✅ Password hashing (bcrypt)
- ✅ SQL injection prevention (prepared statements)
- ✅ CORS configuration (configurable origins)
- ✅ Input validation (JSR-303 annotations)

---

## 🐛 Troubleshooting

### Database Connection Failed
```bash
# Verify PostgreSQL
psql -U postgres -d storeflow -c "SELECT version();"

# Check connection string in application.yml
# spring.datasource.url=jdbc:postgresql://localhost:5432/storeflow
```

### Tests Failing
```bash
# Clear cache and rebuild
mvn clean test -U

# Run single test with debug
mvn test -Dtest=YourTestClass -X
```

### Flyway Migration Issues
```sql
-- Check migration status
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Reset migrations (dev only)
DELETE FROM flyway_schema_history WHERE version > X;
```

---

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes & test: `mvn clean test`
3. Commit with meaningful message
4. Push to GitHub: `git push origin feature/your-feature`
5. Create Pull Request

---

## 📞 Support

- **GitHub Issues:** https://github.com/tejash-sr/Store_Flow_API/issues
- **Documentation:** Check `/docs` folder for detailed guides
- **Email:** tejash@example.com

---

**Latest Release:** v0.9.0 (April 5, 2026)  
**Next Step:** Build minimal frontend with React + Vite

---

### Quick Links

| Resource | URL |
|----------|-----|
| **Swagger UI** | `/swagger-ui.html` |
| **GraphQL IDE** | `/graphiql` |
| **OpenAPI Spec** | `/v3/api-docs` |
| **Health Check** | `/actuator/health` |
| **Metrics** | `/actuator/metrics` |
