package com.storeflow.storeflow_api.graphql;

import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.entity.enums.ProductStatus;
import com.storeflow.storeflow_api.service.CategoryService;
import com.storeflow.storeflow_api.service.ProductService;
import com.storeflow.storeflow_api.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;

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
    private CategoryService categoryService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    private Product testProduct;
    private Category testCategory;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic devices");
        
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setDescription("High-performance laptop");
        testProduct.setSku("LAPTOP-001");
        testProduct.setPrice(1299.99);
        testProduct.setStockQuantity(50);
        testProduct.setStatus(ProductStatus.ACTIVE);
        testProduct.setCategory(testCategory);
        testProduct.setCategoryId(1L);
    }
    
    @Test
    void testProductQuery_ReturnsProductDetails() {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(testProduct);
        
        // Act & Assert
        graphQlTester.documentName("product")
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
        when(productService.getProductById(1L)).thenReturn(testProduct);
        
        // Act & Assert
        graphQlTester.documentName("productWithCategory")
                .variable("id", "1")
                .execute()
                .path("product.category.id").entity(String.class).isEqualTo("1")
                .path("product.category.name").entity(String.class).isEqualTo("Electronics");
        
        verify(productService).getProductById(1L);
    }
    
    @Test
    void testProductQuery_NotFound_ReturnsNull() {
        // Arrange
        when(productService.getProductById(999L)).thenReturn(null);
        
        // Act & Assert
        graphQlTester.documentName("product")
                .variable("id", "999")
                .execute()
                .path("product").entity(Object.class).isNull();
    }
}

/**
 * GraphQL Mutation Tests
 * Tests GraphQL mutations for CRUD operations
 */
@GraphQlTest(MutationResolver.class)
class MutationGraphQlTest {
    
    @Autowired
    private GraphQlTester graphQlTester;
    
    @MockBean
    private ProductService productService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    private Product newProduct;
    
    @BeforeEach
    void setUp() {
        newProduct = new Product();
        newProduct.setId(1L);
        newProduct.setName("New Product");
        newProduct.setSku("NEW-001");
        newProduct.setPrice(99.99);
        newProduct.setStockQuantity(100);
        newProduct.setStatus(ProductStatus.ACTIVE);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteProductMutation_AdminUser_ReturnsTrue() {
        // Arrange
        when(productService.deleteProduct(1L)).thenReturn(true);
        
        // Act & Assert
        graphQlTester.documentName("deleteProduct")
                .variable("id", "1")
                .execute()
                .path("deleteProduct").entity(Boolean.class).isTrue();
        
        verify(productService).deleteProduct(1L);
    }
    
    @Test
    void testDeleteProductMutation_UnauthenticatedUser_ReturnsError() {
        // Act & Assert
        graphQlTester.documentName("deleteProduct")
                .variable("id", "1")
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }
}
