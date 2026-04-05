# OpenAPI/Swagger Documentation Setup

This guide explains the OpenAPI documentation implementation for StoreFlow API.

## Access Documentation

### Swagger UI (Interactive)
- **URL**: http://localhost:8080/swagger-ui.html
- **Features**:
  - Try-it-out functionality for all endpoints
  - Real API calls from browser
  - Bearer token authorization via "Authorize" button
  - Request/response examples
  - Parameter validation

### OpenAPI JSON Schema
- **URL**: http://localhost:8080/v3/api-docs
- **Format**: OpenAPI 3.0 JSON
- **Usage**: Import into Postman, Insomnia, or other API clients

### OpenAPI YAML Schema
- **URL**: http://localhost:8080/v3/api-docs.yaml
- **Format**: OpenAPI 3.0 YAML
- **Usage**: Alternative format for some tools

## Authentication in Swagger UI

### How to Test Protected Endpoints

1. **Get a Bearer Token**:
   - POST `/api/auth/login` (or `/api/auth/signup`)
   - Copy the `accessToken` from response

2. **Authorize in Swagger UI**:
   - Click the green "Authorize" button (top right)
   - Paste: `Bearer YOUR_TOKEN` (include "Bearer " prefix)
   - Click "Authorize" and close dialog

3. **Test Protected Endpoints**:
   - Now you can "Try it out" on any protected endpoint
   - Token is automatically included in request headers

## Documentation Annotations

### Controller Level
```java
@RestController
@RequestMapping("/api/products")
@Tag(
    name = "Products",
    description = "Product management endpoints"
)
public class ProductController { ... }
```

### Endpoint Level
```java
@GetMapping("/{id}")
@Operation(
    summary = "Get product by ID",
    description = "Retrieve detailed information for a specific product",
    operationId = "getProductById",
    tags = {"Products"}
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Product found"),
    @ApiResponse(responseCode = "404", description = "Product not found")
})
public ResponseEntity<?> getProductById(@PathVariable Long id) { ... }
```

### Parameter Documentation
```java
@GetMapping
public ResponseEntity<?> getProducts(
    @Parameter(
        name = "page",
        description = "Page number (0-based)",
        example = "0"
    )
    @RequestParam(defaultValue = "0") Integer page,
    
    @Parameter(
        name = "size",
        description = "Items per page (max 100)",
        example = "20"
    )
    @RequestParam(defaultValue = "20") Integer size
) { ... }
```

### Request Body Documentation
```java
@PostMapping
@Operation(summary = "Create product", security = @SecurityRequirement(name = "bearer-jwt"))
public ResponseEntity<?> createProduct(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Product creation payload",
        content = @Content(
            schema = @Schema(implementation = CreateProductRequest.class),
            examples = @ExampleObject(value = "{ ... JSON example ... }")
        )
    )
    @Valid @RequestBody CreateProductRequest request
) { ... }
```

### DTO Field Documentation
```java
@Data
@Schema(
    name = "CreateProductRequest",
    description = "Request payload for creating a product"
)
public class CreateProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 150)
    @Schema(
        description = "Product name (unique)",
        example = "Wireless Headphones",
        maxLength = 150,
        minLength = 3
    )
    private String name;
    
    @DecimalMin(value = "0.01")
    @Schema(
        description = "Product price in USD",
        example = "299.99",
        type = "number",
        minimum = "0.01"
    )
    private Double price;
}
```

## Keeping Documentation Synchronized

### Automatic Updates
- Documentation auto-generates from code annotations
- Add/update `@Operation`, `@ApiResponse`, `@Parameter` annotations
- Refresh browser to see latest docs

### Best Practices
1. **Always document**:
   - Every controller class with `@Tag`
   - Every endpoint with `@Operation`
   - Every parameter with `@Parameter`
   - Every response path with `@ApiResponse`
   - Every DTO field with `@Schema`

2. **Include examples**:
   - Use `@ExampleObject` for request/response payloads
   - Show realistic data in examples
   - Include edge cases

3. **Document errors**:
   - List all possible HTTP response codes
   - Explain what causes each error
   - Include error response structure

4. **Security**:
   - Mark protected endpoints with `security = @SecurityRequirement(name = "bearer-jwt")`
   - Swagger UI will show lock icon for protected endpoints
   - Users can authorize once for all protected endpoints

## Implementation Checklist

- [x] SpringDoc OpenAPI dependency added (`springdoc-openapi-starter-webmvc-ui`)
- [x] OpenApiConfig bean configured with API info, servers, security schemes
- [x] application.yml configured for Swagger UI at /swagger-ui.html
- [x] Bearer JWT security scheme defined
- [x] Example endpoints documented with @Operation, @ApiResponse, @Parameter
- [x] Example DTOs documented with @Schema on class and fields
- [x] Integration tests verify documentation is accessible
- [ ] Document all remaining controllers (AuthController, OrderController, etc.)
- [ ] Add @Operation to all remaining endpoints
- [ ] Add @Parameter to all query/path parameters
- [ ] Add @ApiResponse to all response paths
- [ ] Add @Schema to all DTO fields
- [ ] Add @ExampleObject to all request/response examples

## Common Annotation Reference

| Annotation | Purpose | Location |
|-----------|---------|----------|
| `@Tag` | Group related endpoints | Controller class |
| `@Operation` | Describe single endpoint | Method |
| `@Parameter` | Document method parameter | Parameter |
| `@RequestBody` | Document request body | Parameter |
| `@ApiResponse` | Document response | Method |
| `@Schema` | Document object/field structure | Class/Field |
| `@ExampleObject` | Show request/response example | @Content |
| `@SecurityRequirement` | Mark endpoint as protected | Method |

## Troubleshooting

### Swagger UI not loading
- Check if springdoc dependency is in pom.xml
- Verify application.yml has springdoc configuration
- Check logs for Spring Boot startup messages

### Endpoints not appearing in Swagger
- Verify @RestController is used (not @Controller)
- Check @RequestMapping path is correct
- Ensure controller is in component scan package

### Bearer token not working in Swagger
- Click "Authorize" button
- Format: `Bearer YOUR_TOKEN` (include "Bearer " prefix)
- Verify token is valid (not expired)
- Check endpoint is protected with `@PreAuthorize`

## References
- [SpringDoc OpenAPI Official Docs](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
