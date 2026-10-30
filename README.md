# StoreFlow API — inventory and order management, built to production standards

> A backend for a multi-store retailer: products, categories, stock, orders, promotions and alerts, behind REST and GraphQL APIs with JWT security, rate limiting, background jobs, email, PDF and CSV exports, and an audit log. Every change runs through **113 automated tests** on GitHub Actions.

Built during backend engineering training · Java 21 · Spring Boot 3 · PostgreSQL · **162 commits in 4 days**

---

## The problem it solves

A retailer with several stores needs one reliable source of truth for **what is in stock, where, and what has been ordered**:

- Stock levels updated in the same database transaction as the order, so an order and its stock change succeed or fail together.
- Alerts **before** a product runs out, not after customers find the shelf empty.
- Order documents (PDF) and exports (CSV) for staff who don't use the API.
- Security that holds up in production: authenticated users, role-based access and protection from abusive clients.

## What it does

| Area | Features |
|---|---|
| **Catalogue** | Products and nested categories, product image upload with automatic thumbnails, advanced search with filters, cursor-based pagination |
| **Inventory** | Stock per store, low-stock alerts |
| **Orders** | Order placement with line items, status lifecycle, order PDF download, CSV export |
| **Promotions** | Promotion management |
| **Security** | JWT sign-up and login, role-based access, per-client rate limiting (Bucket4j) |
| **APIs** | REST, plus GraphQL with nested resolution (products → categories, orders → items) and role checks |
| **Background work** | Async job runner (email, PDF and generic jobs) with status tracking: pending → processing → completed / failed |
| **Email** | Welcome, order confirmation, password reset and admin digest emails (HTML templates, sent asynchronously) |
| **Real time** | WebSocket notifications |
| **Operations** | Health and metrics endpoints (Actuator), audit log, OpenAPI docs with Swagger UI |

## Engineering decisions

| Decision | Why |
|---|---|
| **Database schema under version control** (Flyway, migrations V1–V12) | Every environment builds the same schema from scratch; no manual SQL drift |
| **Integration tests against real PostgreSQL** (Testcontainers) | An in-memory database hides real SQL and constraint behaviour; tests run on the same engine as production |
| **Coverage rule in the build** (JaCoCo: 80% line and branch) | Coverage is a gate, not a report nobody reads |
| **CI on every push** (GitHub Actions with a PostgreSQL service) | A change only counts as done when the full suite passes on a clean machine |
| **Rate limiting at the filter level** (Bucket4j) | Abusive clients are stopped before they reach business logic or the database |
| **Slow work off the request thread** (async jobs for email and PDFs) | API responses stay fast; failures are tracked and visible instead of lost |
| **REST and GraphQL over one service layer** | Clients choose the shape of data they need without duplicating business rules |
| **Shipping address as an embedded value object** (migration V12) | An address belongs to its order and never changes underneath it |

## Quick start

**Prerequisites:** Java 21+, PostgreSQL 15+, Maven 3.9+

```bash
git clone https://github.com/tejash-sr/Store_Flow_API.git
cd Store_Flow_API

export DB_USER=postgres
export DB_PASSWORD=your_password
export DB_NAME=storeflow

mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

| Open | What |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interactive REST documentation (every endpoint, request and response) |
| `http://localhost:8080/graphiql` | GraphQL explorer |
| `http://localhost:8080/actuator/health` | Health check |

**Main REST routes:** `/api/auth` (signup, login) · `/api/products` · `/api/orders` · `/api/alerts` · `/api/v1/promotions` · `/api/v1/audit-logs` · `/api/health`

### Authentication

```bash
POST /api/auth/signup     # create an account
POST /api/auth/login      # { "email": "...", "password": "..." }  →  JWT

# then, on every request:
Authorization: Bearer <token>
```

## Tests

```bash
mvn clean test                       # 113 tests in 27 test classes
mvn test -Dtest=OrderControllerTest  # one class
mvn clean verify                     # tests + JaCoCo coverage rule
```

Tests cover controllers, services, repositories (against PostgreSQL via Testcontainers), specifications, validation, security configuration, email (GreenMail) and metrics. The same suite runs on every push in GitHub Actions.

## Architecture

```
src/main/java/com/storeflow/storeflow_api/
  config/          security, GraphQL, OpenAPI, async
  controller/      REST endpoints
  graphql/         GraphQL resolvers
  service/         business logic, email, PDF, CSV, async jobs
  repository/      data access and custom queries
  specification/   dynamic search filters
  entity/          JPA entities and enums
  dto/             request and response objects
  security/        JWT filter, rate limiting
  validation/      custom validators
  metrics/         custom metrics
src/main/resources/
  db/migration/    Flyway migrations V1–V12
  graphql/         schema
```

**Stack:** Spring Boot 3.2 · Spring Security (JWT) · Spring Data JPA · Spring GraphQL · PostgreSQL · Flyway · Bucket4j · PDFBox · Thumbnailator · Commons CSV · SpringDoc OpenAPI · JUnit 5 · Mockito · Testcontainers · GreenMail · JaCoCo · Docker

## Deployment

```bash
mvn clean package -DskipTests
docker build -t storeflow-api .
docker run -e SPRING_PROFILES_ACTIVE=prod -e DB_HOST=postgres -p 8080:8080 storeflow-api
```

Or run the whole stack with `docker compose up`. An `nginx.conf` is included for running behind a reverse proxy.

**Production environment variables**

```bash
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
MAIL_HOST, MAIL_PORT, MAIL_USER, MAIL_PASSWORD
JWT_SECRET
```

Flyway applies all migrations automatically on startup.
