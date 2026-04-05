package com.storeflow.storeflow_api.graphql;

import com.storeflow.storeflow_api.dto.ProductResponse;
import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.enums.ProductStatus;
import com.storeflow.storeflow_api.service.ProductService;
import com.storeflow.storeflow_api.service.OrderService;
import com.storeflow.storeflow_api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GraphQL Product Query Tests
 * Tests GraphQL resolvers for product queries
 */
@GraphQlTest(ProductResolver.class)
class ProductGraphQlTest {
    
    @Autowired
    private GraphQlTester graphQlTester;
    
    @MockBean
    private ProductService productService;

    @MockBean
    private OrderService orderService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    private ProductResponse testProduct;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic devices");
        
                testProduct = ProductResponse.builder()
                                .id(1L)
                                .name("Laptop")
                                .description("High-performance laptop")
                                .sku("LAPTOP-001")
                                .price(new java.math.BigDecimal("1299.99"))
                                .stockQuantity(50L)
                                .categoryName("Electronics")
                                .categoryId(1L)
                                .category(testCategory)
                                .status(ProductStatus.ACTIVE)
                                .build();
    }
    
    @Test
    void testProductQuery_ReturnsProductDetails() {
        // Arrange
                when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));
        
        // Act & Assert
                graphQlTester.document("""
                                query($id: ID!) {
                                    product(id: $id) {
                                        id
                                        name
                                        sku
                                        price
                                        stockQuantity
                                    }
                                }
                                """)
                .variable("id", "1")
                .execute()
                .path("product.id").entity(String.class).isEqualTo("1")
                .path("product.name").entity(String.class).isEqualTo("Laptop")
                .path("product.sku").entity(String.class).isEqualTo("LAPTOP-001")
                .path("product.price").entity(Double.class).isEqualTo(1299.99)
                .path("product.stockQuantity").entity(int.class).isEqualTo(50);
        
        verify(productService).getProductById(1L);
    }
    
    @Test
    void testProductCategoryField_ResolvesCategory() {
        // Arrange
                when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));
        
        // Act & Assert
                graphQlTester.document("""
                                query($id: ID!) {
                                    product(id: $id) {
                                        category {
                                            id
                                            name
                                        }
                                    }
                                }
                                """)
                .variable("id", "1")
                .execute()
                .path("product.category.id").entity(String.class).isEqualTo("1")
                .path("product.category.name").entity(String.class).isEqualTo("Electronics");
        
        verify(productService).getProductById(1L);
    }
    
    @Test
    void testProductQuery_NotFound_ReturnsNull() {
        // Arrange
                when(productService.getProductById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
                graphQlTester.document("""
                                query($id: ID!) {
                                    product(id: $id) {
                                        id
                                    }
                                }
                                """)
                .variable("id", "999")
                .execute()
                .path("product").valueIsNull();
    }
}

/**
 * GraphQL Mutation Tests
 * Tests GraphQL mutations for CRUD operations
 */
@GraphQlTest(MutationResolver.class)
@Import(MutationGraphQlTest.MethodSecurityTestConfig.class)
class MutationGraphQlTest {
    
    @Autowired
    private GraphQlTester graphQlTester;
    
    @MockBean
    private ProductService productService;

    @MockBean
    private OrderService orderService;
    
    @MockBean
    private JwtUtil jwtUtil;

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity(prePostEnabled = true)
    static class MethodSecurityTestConfig {
    }
    
    private ProductResponse newProduct;
    
    @BeforeEach
    void setUp() {
        newProduct = ProductResponse.builder()
            .id(1L)
            .name("New Product")
            .sku("NEW-001")
            .price(new java.math.BigDecimal("99.99"))
            .stockQuantity(100L)
            .status(ProductStatus.ACTIVE)
            .build();
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteProductMutation_AdminUser_ReturnsTrue() {
        // Arrange
        doNothing().when(productService).deleteProduct(1L);
        
        // Act & Assert
        graphQlTester.document("""
            mutation($id: ID!) {
              deleteProduct(id: $id)
            }
            """)
                .variable("id", "1")
                .execute()
                .path("deleteProduct").entity(Boolean.class).isEqualTo(true);
        
        verify(productService).deleteProduct(1L);
    }
    
    @Test
    void testDeleteProductMutation_UnauthenticatedUser_ReturnsError() {
        // Act & Assert
        graphQlTester.document("""
            mutation($id: ID!) {
              deleteProduct(id: $id)
            }
            """)
                .variable("id", "1")
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }
}
