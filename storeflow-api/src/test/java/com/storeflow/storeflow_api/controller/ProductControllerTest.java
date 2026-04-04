package com.storeflow.storeflow_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storeflow.storeflow_api.config.TestMailConfig;
import com.storeflow.storeflow_api.dto.ProductRequest;
import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.repository.CategoryRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = categoryRepository.save(Category.builder()
            .name("Electronics")
            .description("Electronic devices")
            .isActive(true)
            .build());

        // Create test product
        testProduct = productRepository.save(Product.builder()
            .name("Test Product")
            .sku("TST001")
            .price(BigDecimal.valueOf(99.99))
            .category(testCategory)
            .isActive(true)
            .build());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateProductSuccess() throws Exception {
        ProductRequest request = ProductRequest.builder()
            .name("New Product")
            .description("A new test product")
            .sku("NEW001")
            .price(BigDecimal.valueOf(49.99))
            .categoryId(testCategory.getId())
            .build();

        mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("New Product"))
            .andExpect(jsonPath("$.sku").value("NEW001"));
    }

    @Test
    void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/products")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testGetProductByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/products/" + testProduct.getId())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/products/99999")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateProductSuccess() throws Exception {
        ProductRequest updateRequest = ProductRequest.builder()
            .name("Updated Product")
            .price(BigDecimal.valueOf(129.99))
            .build();

        mockMvc.perform(put("/api/products/" + testProduct.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Product"))
            .andExpect(jsonPath("$.price").value(129.99));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteProductSuccess() throws Exception {
        mockMvc.perform(delete("/api/products/" + testProduct.getId())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Verify soft delete (product marked as inactive)
        Product deleted = productRepository.findById(testProduct.getId()).orElse(null);
        assert deleted != null;
        assert !deleted.getIsActive();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdjustStockSuccess() throws Exception {
        mockMvc.perform(patch("/api/products/" + testProduct.getId() + "/stock?quantity=10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
