package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.config.TestMailConfig;
import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.entity.Store;
import com.storeflow.storeflow_api.repository.CategoryRepository;
import com.storeflow.storeflow_api.repository.OrderRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import com.storeflow.storeflow_api.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for file upload/download endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class FileOperationsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Category testCategory;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Create category
        testCategory = Category.builder()
            .name("Electronics")
            .description("Electronic products")
            .build();
        testCategory = categoryRepository.save(testCategory);

        // Create product
        testProduct = Product.builder()
            .name("Test Product")
            .sku("TEST-SKU-001")
            .description("Test product description")
            .price(BigDecimal.valueOf(99.99))
            .category(testCategory)
            .build();
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testUploadProductImage_ValidFile_Returns200() throws Exception {
        byte[] imageContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        MockMultipartFile file = new MockMultipartFile(
            "file", "product.jpg", "image/jpeg", imageContent);

        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId())
            .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Image uploaded successfully"));
    }

    @Test
    void testUploadProductImage_FileTooLarge_Returns400() throws Exception {
        byte[] largeContent = new byte[6 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
            "file", "large.jpg", "image/jpeg", largeContent);

        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId())
            .file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("5MB")));
    }

    @Test
    void testUploadProductImage_InvalidMimeType_Returns400() throws Exception {
        byte[] textContent = "This is not an image".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "text.txt", "text/plain", textContent);

        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId())
            .file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Invalid file type")));
    }

    @Test
    void testUploadProductImage_ProductNotFound_Returns404() throws Exception {
        byte[] imageContent = new byte[]{(byte) 0xFF, (byte) 0xD8};
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", imageContent);

        mockMvc.perform(multipart("/api/products/{id}/image", 99999L)
            .file(file))
            .andExpect(status().isNotFound());
    }

    @Test
    void testUploadProductImage_NoFile_Returns400() throws Exception {
        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadProductImage_PngFile_Returns200() throws Exception {
        byte[] pngContent = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        MockMultipartFile file = new MockMultipartFile(
            "file", "product.png", "image/png", pngContent);

        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId())
            .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Image uploaded successfully"));
    }

}
