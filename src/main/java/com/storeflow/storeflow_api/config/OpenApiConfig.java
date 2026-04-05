package com.storeflow.storeflow_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI/Swagger Configuration
 * Configures API documentation with:
 * - OpenAPI 3.0 spec
 * - JWT Bearer token security scheme
 * - API title, description, and contact info
 * - Server URLs for dev/prod
 * 
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access OpenAPI JSON at: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StoreFlow API")
                        .version("0.8.0")
                        .description("Production-grade Inventory & Order Management REST API\n\n" +
                                "Features:\n" +
                                "- JWT-based authentication with access & refresh tokens\n" +
                                "- Full CRUD operations for products, orders, and users\n" +
                                "- Role-based access control (USER, ADMIN)\n" +
                                "- File upload/download with image resizing\n" +
                                "- PDF order reports and CSV exports\n" +
                                "- WebSocket real-time order notifications\n" +
                                "- Email notifications (welcome, password reset, orders)\n" +
                                "- Advanced product search with pagination\n" +
                                "- Rate limiting and request tracing\n" +
                                "- Actuator metrics and Prometheus integration\n\n" +
                                "**Security**: All protected endpoints require Bearer token in Authorization header.")
                        .contact(new Contact()
                                .name("StoreFlow Team")
                                .email("support@storeflow.local")
                                .url("https://github.com/tejash-sr/StoreFlowAPI"))
                        .license(new License()
                                .name("Internal Use - Grootan Technologies")
                                .url("https://www.grootan.com")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development"),
                        new Server()
                                .url("https://api.storeflow.local")
                                .description("Production")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token in the format: Bearer YOUR_JWT_TOKEN\n\n" +
                                        "To get a token:\n" +
                                        "1. POST /api/auth/signup or /api/auth/login\n" +
                                        "2. Copy the 'accessToken' from the response\n" +
                                        "3. Click the lock icon above and paste: Bearer YOUR_TOKEN\n" +
                                        "4. All Bearer token protected requests will include your token")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
