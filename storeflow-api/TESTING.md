# Testing Guide for StoreFlow API

This document explains how to run tests locally and in CI/CD.

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (for PostgreSQL)

## Local Testing Setup

### Step 1: Start PostgreSQL with Docker Compose

```bash
cd storeflow-api
docker-compose up -d
```

This will start:
- **Production DB**: `storeflow` on localhost:5432
- **Test DB**: `storeflow_test` on localhost:5433

### Step 2: Verify PostgreSQL Connection

```bash
# Check if PostgreSQL is running
docker-compose ps

# Connect to test database (optional)
psql -h localhost -p 5433 -U storeflow -d storeflow_test
```

Password: `storeflow123`

### Step 3: Run Tests

```bash
cd storeflow-api

# Run all tests
./mvnw clean test

# Run specific test class
./mvnw test -Dtest=CategoryRepositoryTest

# Run with coverage
./mvnw clean test jacoco:report
```

### Step 4: View Test Results

Test reports are generated in:
- `target/surefire-reports/` - XML test results
- `target/site/jacoco/` - Code coverage report

## Test Configuration

The test profile (`application-test.yml`) is automatically used when tests run:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/storeflow_test
    username: storeflow
    password: storeflow123
  jpa:
    hibernate:
      ddl-auto: create-drop  # Auto-create schema, drop after tests
```

## CI/CD Pipeline (GitHub Actions)

The GitHub Actions workflow (`.github/workflows/maven-test.yml`):
- ✅ Spins up PostgreSQL service container automatically
- ✅ Builds project with Maven
- ✅ Runs all tests
- ✅ Generates test reports
- ✅ Uploads artifacts

**Branches that trigger CI/CD:**
- `main`
- `develop`
- `hotfix/**`
- `feature/**`
- `phase-*`

## Troubleshooting

### PostgreSQL Connection Refused

```bash
# Check if postgres is running
docker-compose ps

# Restart PostgreSQL
docker-compose restart postgres postgres-test

# Check logs
docker-compose logs postgres
```

### Port Already in Use

Change ports in `docker-compose.yml`:
```yaml
postgres:
  ports:
    - "5432:5432"  # Change first number to unused port

postgres-test:
  ports:
    - "5433:5432"  # Change first number to unused port
```

### Tests Still Failing

1. **Check database credentials** in `application-test.yml`
2. **View test output**: `./mvnw test -e` (verbose error output)
3. **Check PostgreSQL logs**: `docker-compose logs postgres`
4. **Clean rebuild**: `./mvnw clean install -DskipTests`

## Running Tests in Different Profiles

### Development (uses prod DB)
```bash
./mvnw test -Dspring.profiles.active=dev
```

### Test Profile (uses test DB)
```bash
./mvnw test -Dspring.profiles.active=test  # Default
```

## Test Statistics

Check test results:
```bash
# Count tests
find target/surefire-reports -name "*.xml" | wc -l

# View summary
grep "Tests run:" target/surefire-reports/*.txt
```

## Best Practices

✅ Always start PostgreSQL before running tests  
✅ Use `docker-compose down` when done testing  
✅ Commit test changes only to feature/phase branches  
✅ Run tests locally before pushing to remote  
✅ Check GitHub Actions pipeline after push  

## Cleanup

```bash
# Stop PostgreSQL containers
docker-compose down

# Remove data volumes (fresh start next time)
docker-compose down -v

# Remove test artifacts
./mvnw clean
```
