# Setting Up PostgreSQL for StoreFlow API

Alright, so we're using PostgreSQL for this project. Here's how to get it running locally for development and testing.

## What You Need

- Docker & Docker Compose (easiest option)
- Or a local PostgreSQL 15+ installation if you prefer

We recommend Docker - it's literally one command and you're done.

## Quick Start (Docker)

### 1. Start the databases

```bash
cd storeflow-api
docker-compose up -d
```

That's it. Two databases will be created:
- **storeflow** (port 5432) - for development
- **storeflow_test** (port 5433) - for running tests

### 2. Verify it's running

```bash
docker-compose ps
```

You should see two containers running. If you see errors, check Docker is actually installed and running.

### 3. Test the connection

```bash
# Connect to test database just to make sure
psql -h localhost -p 5433 -U storeflow -d storeflow_test
```

Password is `storeflow123`. If you get in, type `\q` to exit. If you don't have `psql` installed, that's fine - the app will use it automatically.

## Credentials

```
Username: storeflow
Password: storeflow123
Dev Database: storeflow (port 5432)
Test Database: storeflow_test (port 5433)
```

## When You're Done

```bash
docker-compose down
```

This stops the containers but keeps the data. If you want a fresh start next time:

```bash
docker-compose down -v
```

The `-v` removes the data volumes too.

## Common Issues

### "Port 5432 is already in use"

You probably have PostgreSQL running already. Either:

1. Stop the other PostgreSQL instance, or
2. Change the port in `docker-compose.yml`:

```yaml
postgres:
  ports:
    - "15432:5432"  # Use 15432 instead of 5432
```

Then you'd connect with `psql -p 15432 ...`

### "Docker daemon not running"

Make sure Docker Desktop is actually open and running. On Windows, it should be in your system tray.

### "psql: command not found"

You don't have PostgreSQL tools installed. That's okay - the app doesn't need them. It uses JDBC internally. Skip that test.

## For CI/CD (GitHub Actions)

The GitHub Actions workflow automatically spins up PostgreSQL in a container during tests. You don't need to do anything - it just works.

## Local vs Test Database

When you run the app normally, it uses the **storeflow** database (port 5432).

When you run tests with `./mvnw test`, it automatically uses the **storeflow_test** database (port 5433) with `create-drop` mode (schema is created fresh, then dropped after tests).

This keeps your dev data safe from tests messing it up.
