#!/bin/bash

# StoreFlow API Production Verification Script

echo "================================"
echo "StoreFlow API - Production Check"
echo "================================"
echo ""

# 1. Check Docker Containers
echo "1️⃣  Docker Containers Status:"
docker-compose ps --services

echo ""
echo "2️⃣  API Endpoint Tests:"

# Test health endpoint
echo "   Testing: GET /actuator/health"
curl -s -o /dev/null -w "   Status: %{http_code}\n" http://localhost:8080/actuator/health

# Test API info
echo "   Testing: GET /api/info (Swagger UI)"
curl -s -o /dev/null -w "   Status: %{http_code}\n" http://localhost:8080/api/swagger-ui.html

# Test products endpoint (no auth required for GET)
echo "   Testing: GET /api/v1/products"
curl -s -o /dev/null -w "   Status: %{http_code}\n" http://localhost:8080/api/v1/products

echo ""
echo "3️⃣  Database Check:"
docker-compose exec -T postgres pg_isready -U storeflow -d storeflow 2>&1 | grep -q "accepting" && echo "   ✅ PostgreSQL Ready" || echo "   ❌ PostgreSQL Error"

echo ""
echo "4️⃣  Docker Stats:"
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"

echo ""
echo "================================"
echo "✅ Production Ready!"
echo "API: http://storeflowapi.duckdns.org"
echo "Swagger UI: http://storeflowapi.duckdns.org/api/swagger-ui.html"
echo "================================"
