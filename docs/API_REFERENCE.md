# StoreFlow API — API Reference

> **Version:** 1.0  
> **Repository:** https://github.com/tejash-sr/StoreFlowAPI  
> **Base URL:** `http://localhost:8080`
> **Auth:** Bearer JWT (in `Authorization` header)  
> **Content-Type:** `application/json` (unless noted)

---

## Table of Contents

1. [Auth Endpoints](#1-auth-endpoints)
2. [Product Endpoints](#2-product-endpoints)
3. [Order Endpoints](#3-order-endpoints)
4. [File & Report Endpoints](#4-file--report-endpoints)
5. [Admin Endpoints](#5-admin-endpoints)
6. [Health & Actuator Endpoints](#6-health--actuator-endpoints)
7. [WebSocket / STOMP Reference](#7-websocket--stomp-reference)
8. [Error Response Reference](#8-error-response-reference)
9. [Common DTOs](#9-common-dtos)

---

## 1. Auth Endpoints

---

### POST /api/auth/signup

Register a new user account.

**Auth Required:** No  
**Rate Limit:** 5 req/15 min per IP

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password1!",
  "fullName": "Jane Doe"
}
```

**Validation:**
- `email`: required, valid email format
- `password`: required, min 8 chars
- `fullName`: required, 2-100 chars

**Success Response (201 Created):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "user@example.com",
    "fullName": "Jane Doe",
    "role": "USER",
    "avatarUrl": null,
    "createdAt": "2026-03-31T12:00:00Z"
  }
}
```

**Error Responses:**
| Code | Reason |
|------|--------|
| 400 | Validation failure (see `errors` field) |
| 409 | Email already registered |

---

### POST /api/auth/login

Authenticate and receive JWT tokens.

**Auth Required:** No

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password1!"
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Error Responses:**
| Code | Reason |
|------|--------|
| 400 | Missing/invalid fields |
| 401 | Invalid email or password |

---

### POST /api/auth/refresh

Get a new access token using a valid refresh token.

**Auth Required:** No

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 900
}
```

**Error Responses:**
| Code | Reason |
|------|--------|
| 401 | Refresh token expired or invalid |

---

### POST /api/auth/forgot-password

Request a password reset email.

**Auth Required:** No

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Success Response (200 OK):**
```json
{
  "message": "If that email exists, a reset link has been sent."
}
```

*Note: Always returns 200 to prevent email enumeration attacks.*

---

### POST /api/auth/reset-password/{token}

Reset password using the token from the email.

**Auth Required:** No

**Path Parameter:** `token` — the reset token from the email

**Request Body:**
```json
{
  "newPassword": "NewPassword1!"
}
```

**Success Response (200 OK):**
```json
{
  "message": "Password reset successful. You may now log in."
}
```

**Error Responses:**
| Code | Reason |
|------|--------|
| 400 | Token expired or invalid |

---

### GET /api/auth/me

Get the current authenticated user's profile.

**Auth Required:** Yes (USER or ADMIN)

**Success Response (200 OK):**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "user@example.com",
  "fullName": "Jane Doe",
  "role": "USER",
  "avatarUrl": "http://localhost:8080/api/auth/me/avatar",
  "enabled": true,
  "createdAt": "2026-03-31T12:00:00Z"
}
```

---

### PUT /api/auth/me/avatar

Upload or replace the current user's avatar image.

**Auth Required:** Yes (USER or ADMIN)  
**Content-Type:** `multipart/form-data`

**Form Data:**
- `avatar` (file): Image file. Max 5MB. Types: `image/jpeg`, `image/png`, `image/webp`

**Success Response (200 OK):**
```json
{
  "avatarUrl": "http://localhost:8080/api/auth/me/avatar"
}
```

---

## 2. Product Endpoints

---

### POST /api/products

Create a new product.

**Auth Required:** Yes (ADMIN only)

**Request Body:**
```json
{
  "name": "MacBook Pro 16\"",
  "description": "Apple MacBook Pro with M3 Max chip, 16-inch Liquid Retina XDR display.",
  "sku": "APPLE-MBP16-M3",
  "price": 3499.00,
  "stockQuantity": 25,
  "categoryId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

**Validation:**
- `name`: required, 3-150 chars
- `description`: required, max 3000 chars
- `sku`: required, pattern `^[A-Z0-9-]+$`
- `price`: required, positive decimal
- `stockQuantity`: non-negative integer
- `categoryId`: must reference an existing category

**Success Response (201 Created):**
```json
{
  "id": "7c956f11-b1a0-4671-a7a0-fd55ef4572b1",
  "name": "MacBook Pro 16\"",
  "description": "...",
  "sku": "APPLE-MBP16-M3",
  "price": 3499.00,
  "stockQuantity": 25,
  "status": "ACTIVE",
  "imageUrl": null,
  "category": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "name": "Electronics"
  },
  "createdAt": "2026-03-31T12:00:00Z",
  "updatedAt": "2026-03-31T12:00:00Z"
}
```

---

### GET /api/products

List products with optional filters and pagination.

**Auth Required:** No

**Query Parameters:**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 20 | Page size (max 100) |
| `sort` | string | `createdAt,desc` | Sort field and direction |
| `category` | UUID | - | Filter by category ID |
| `status` | string | ACTIVE | Filter by status (ACTIVE/INACTIVE) |
| `minPrice` | decimal | - | Minimum price filter |
| `maxPrice` | decimal | - | Maximum price filter |
| `name` | string | - | Partial, case-insensitive name search |

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "7c956f11-...",
      "name": "MacBook Pro 16\"",
      "sku": "APPLE-MBP16-M3",
      "price": 3499.00,
      "stockQuantity": 25,
      "status": "ACTIVE",
      "category": { "id": "...", "name": "Electronics" },
      "imageUrl": null,
      "createdAt": "2026-03-31T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "hasNext": false
}
```

---

### GET /api/products/{id}

Get a single product by ID with its category.

**Auth Required:** No

**Path Parameter:** `id` — Product UUID

**Success Response (200 OK):** Full product object (see POST response above)

**Error Response:** 404 if not found

---

### PUT /api/products/{id}

Full update of a product's details.

**Auth Required:** Yes (ADMIN only)

**Request Body:** Same as POST (all fields required)

**Success Response (200 OK):** Updated product object

---

### PATCH /api/products/{id}/stock

Adjust the stock quantity (increment or decrement).

**Auth Required:** Yes (ADMIN only)

**Request Body:**
```json
{
  "adjustment": -5,
  "reason": "Damaged units removed"
}
```

- `adjustment`: Integer, can be positive (add) or negative (remove)
- Result must not go below 0 — returns 409 if it would

**Success Response (200 OK):**
```json
{
  "id": "7c956f11-...",
  "sku": "APPLE-MBP16-M3",
  "previousStock": 25,
  "adjustment": -5,
  "newStock": 20
}
```

---

### DELETE /api/products/{id}

Soft-delete a product (sets status to DISCONTINUED, preserves data for order history).

**Auth Required:** Yes (ADMIN only)

**Success Response (204 No Content)**

---

### POST /api/products/{id}/image

Upload an image for a product.

**Auth Required:** Yes (ADMIN only)  
**Content-Type:** `multipart/form-data`

**Form Data:**
- `image` (file): Image file. Max 5MB. Types: `image/jpeg`, `image/png`, `image/webp`

**Success Response (200 OK):**
```json
{
  "imageUrl": "http://localhost:8080/api/products/7c956f11-.../image"
}
```

---

### GET /api/products/{id}/image

Download (stream) the product image.

**Auth Required:** No

**Success Response (200 OK):**  
Binary image data with `Content-Type: image/jpeg` (or appropriate type)

---

## 3. Order Endpoints

---

### POST /api/orders

Place a new order. Validates stock, deducts inventory, calculates total.

**Auth Required:** Yes (USER or ADMIN)

**Request Body:**
```json
{
  "items": [
    {
      "productId": "7c956f11-b1a0-4671-a7a0-fd55ef4572b1",
      "quantity": 2
    }
  ],
  "shippingAddress": {
    "street": "123 Main Street",
    "city": "San Francisco",
    "country": "USA",
    "postalCode": "94105"
  }
}
```

**Validation:**
- `items`: required, non-empty list
- Each item: `productId` required, `quantity` positive integer
- `shippingAddress`: all fields required

**Success Response (201 Created):**
```json
{
  "id": "a1b2c3d4-...",
  "referenceNumber": "ORD-2026-00001",
  "status": "PENDING",
  "totalAmount": 6998.00,
  "items": [
    {
      "productId": "7c956f11-...",
      "productName": "MacBook Pro 16\"",
      "sku": "APPLE-MBP16-M3",
      "quantity": 2,
      "unitPrice": 3499.00,
      "subtotal": 6998.00
    }
  ],
  "shippingAddress": {
    "street": "123 Main Street",
    "city": "San Francisco",
    "country": "USA",
    "postalCode": "94105"
  },
  "createdAt": "2026-03-31T12:00:00Z"
}
```

**Error Responses:**
| Code | Reason |
|------|--------|
| 400 | Validation failure |
| 404 | Product not found |
| 409 | Insufficient stock (includes which product) |

---

### GET /api/orders

List orders. Users see only their own; Admins see all.

**Auth Required:** Yes

**Query Parameters:**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | 0 | Page number |
| `size` | int | 20 | Page size |
| `status` | string | - | Filter by order status |
| `from` | ISO date | - | Filter orders created after this date |
| `to` | ISO date | - | Filter orders created before this date |

**Success Response (200 OK):** Paginated list of order summaries

---

### GET /api/orders/{id}

Get a single order with all items and product details.

**Auth Required:** Yes  
**Ownership:** Users can only access their own orders (403 otherwise)

**Success Response (200 OK):** Full order object (see POST response above)

---

### PATCH /api/orders/{id}/status

Update an order's status (admin only). Validates legal state transitions.

**Auth Required:** Yes (ADMIN only)

**Request Body:**
```json
{
  "status": "CONFIRMED",
  "note": "Payment verified, order confirmed"
}
```

**Valid Transitions:**
```
PENDING → CONFIRMED
CONFIRMED → SHIPPED
SHIPPED → DELIVERED
PENDING → CANCELLED
CONFIRMED → CANCELLED
```

**Success Response (200 OK):**
```json
{
  "id": "a1b2c3d4-...",
  "referenceNumber": "ORD-2026-00001",
  "previousStatus": "PENDING",
  "newStatus": "CONFIRMED",
  "updatedAt": "2026-03-31T12:05:00Z"
}
```

**Error Responses:**
| Code | Reason |
|------|--------|
| 404 | Order not found |
| 422 | Invalid status transition |

---

## 4. File & Report Endpoints

---

### GET /api/orders/{id}/report

Generate and stream an order summary PDF.

**Auth Required:** Yes (USER gets own order; ADMIN gets any)

**Success Response (200 OK):**  
Binary PDF data  
`Content-Type: application/pdf`  
`Content-Disposition: attachment; filename="order-ORD-2026-00001.pdf"`

**PDF Contains:**
- StoreFlow header
- Order reference number
- Customer name and email
- Order date
- Itemized line items (name, SKU, qty, unit price, subtotal)
- Order total
- Shipping address

---

### GET /api/orders/export

Export orders as a CSV file filtered by date range.

**Auth Required:** Yes (ADMIN only)

**Query Parameters:**

| Param | Type | Required | Description |
|-------|------|---------|-------------|
| `from` | ISO date | Yes | Start date (e.g., `2026-01-01`) |
| `to` | ISO date | Yes | End date (e.g., `2026-03-31`) |

**Success Response (200 OK):**  
CSV file  
`Content-Type: text/csv`  
`Content-Disposition: attachment; filename="orders-export.csv"`

**CSV Format:**
```
OrderRef,CustomerEmail,Status,ProductName,SKU,Quantity,UnitPrice,Subtotal,OrderTotal,OrderDate
ORD-2026-00001,user@example.com,CONFIRMED,MacBook Pro,APPLE-MBP16-M3,2,3499.00,6998.00,6998.00,2026-03-31
```

---

## 5. Admin Endpoints

---

### GET /api/admin/products/low-stock

Get products with stock quantity below the specified threshold.

**Auth Required:** Yes (ADMIN only)

**Query Parameters:**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `threshold` | int | 10 | Stock quantity threshold |
| `page` | int | 0 | Page number |
| `size` | int | 20 | Page size |

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "7c956f11-...",
      "name": "MacBook Pro 16\"",
      "sku": "APPLE-MBP16-M3",
      "stockQuantity": 3,
      "threshold": 10,
      "category": { "id": "...", "name": "Electronics" }
    }
  ],
  "totalElements": 5,
  "page": 0,
  "size": 20
}
```

---

## 6. Health & Actuator Endpoints

---

### GET /api/health

Application health check.

**Auth Required:** No

**Success Response (200 OK):**
```json
{
  "status": "UP",
  "timestamp": "2026-03-31T12:00:00Z",
  "jvmUptimeMs": 120000
}
```

---

### GET /actuator/health

Spring Boot Actuator health endpoint.

**Auth Required:** No (shows basic status) / ADMIN (shows full details)

**Success Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL" } },
    "diskSpace": { "status": "UP" }
  }
}
```

---

### GET /actuator/metrics

Metrics endpoint (requires ADMIN).

### GET /actuator/prometheus

Prometheus metrics scrape endpoint (requires auth in prod).

---

## 7. WebSocket / STOMP Reference

### Connect

```
URL: ws://localhost:8080/ws
SockJS fallback: http://localhost:8080/ws
Auth: ?token=<JWT> query parameter OR Authorization: Bearer <JWT> header
```

### Subscribe: Order Status (public per order)

```
Destination: /topic/orders/{orderId}/status

Message payload:
{
  "orderId": "a1b2c3d4-...",
  "referenceNumber": "ORD-2026-00001",
  "previousStatus": "PENDING",
  "newStatus": "CONFIRMED",
  "timestamp": "2026-03-31T12:05:00Z"
}
```

### Subscribe: Personal Notifications

```
Destination: /user/queue/notifications

Message payload:
{
  "type": "ORDER_STATUS_CHANGED",
  "orderId": "a1b2c3d4-...",
  "referenceNumber": "ORD-2026-00001",
  "newStatus": "CONFIRMED",
  "message": "Your order ORD-2026-00001 has been confirmed.",
  "timestamp": "2026-03-31T12:05:00Z"
}
```

---

## 8. Error Response Reference

### Standard Error Shape

```json
{
  "timestamp": "2026-03-31T12:00:00Z",
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

### HTTP Status Code Summary

| Code | Description | Common Triggers |
|------|-------------|----------------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST (resource created) |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation failure, malformed JSON |
| 401 | Unauthorized | Missing/expired/invalid JWT |
| 403 | Forbidden | Insufficient role, accessing another user's resource |
| 404 | Not Found | Resource with given ID does not exist |
| 409 | Conflict | Duplicate SKU/email, insufficient stock |
| 422 | Unprocessable Entity | Invalid order status transition |
| 429 | Too Many Requests | Rate limit exceeded on auth endpoints |
| 500 | Internal Server Error | Unexpected server error |

---

## 9. Common DTOs

### ShippingAddress

```json
{
  "street": "123 Main Street",
  "city": "San Francisco",
  "country": "USA",
  "postalCode": "94105"
}
```

### Category

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Electronics",
  "description": "Consumer electronics and gadgets",
  "parentId": null,
  "status": "ACTIVE"
}
```

### OrderStatus Enum Values

| Value | Description |
|-------|-------------|
| `PENDING` | Order placed, awaiting confirmation |
| `CONFIRMED` | Order confirmed, preparing for shipment |
| `SHIPPED` | Order dispatched |
| `DELIVERED` | Order delivered to customer |
| `CANCELLED` | Order cancelled |

### ProductStatus Enum Values

| Value | Description |
|-------|-------------|
| `ACTIVE` | Available for purchase |
| `INACTIVE` | Temporarily unavailable |
| `DISCONTINUED` | Soft-deleted, no longer available |

### Role Enum Values

| Value | Permissions |
|-------|------------|
| `USER` | Place orders, read products, manage own profile |
| `ADMIN` | All USER permissions + manage products, update order status, access admin endpoints |
