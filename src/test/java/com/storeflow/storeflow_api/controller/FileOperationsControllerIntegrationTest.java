package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.AbstractIntegrationTest;
import com.storeflow.storeflow_api.entity.*;
import com.storeflow.storeflow_api.repository.CategoryRepository;
import com.storeflow.storeflow_api.repository.OrderRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import com.storeflow.storeflow_api.repository.StoreRepository;
import com.storeflow.storeflow_api.repository.UserRepository;
import com.storeflow.storeflow_api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for file upload/download endpoints
 */
@Transactional
class FileOperationsControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private Category testCategory;
    private Product testProduct;
    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = User.builder()
            .email("test@example.com")
            .password(passwordEncoder.encode("password123"))
            .fullName("Test User")
            .roles(new HashSet<>(List.of(UserRole.ROLE_ADMIN)))
            .status(UserStatus.ACTIVE)
            .build();
        testUser = userRepository.save(testUser);

        // Generate JWT token for authenticated requests
        jwtToken = jwtUtil.generateAccessToken(testUser.getEmail(), testUser.getId().toString(), 
            testUser.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toSet()));

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
    void testUploadProductImage_ValidJpegFile_Returns200() throws Exception {
        byte[] imageContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        MockMultipartFile file = new MockMultipartFile(
            "file", "product.jpg", "image/jpeg", imageContent);

        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId())
            .file(file)
            .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk());
    }

    @Test
    void testUploadProductImage_ValidPngFile_Returns200() throws Exception {
        byte[] pngContent = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        MockMultipartFile file = new MockMultipartFile(
            "file", "product.png", "image/png", pngContent);

        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId())
            .file(file)
            .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk());
    }

    @Test
    void testUploadProductImage_FileTooLarge_Returns400() throws Exception {
        byte[] largeContent = new byte[6 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
            "file", "large.jpg", "image/jpeg", largeContent);

        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId())
            .file(file)
            .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadProductImage_InvalidMimeType_Returns400() throws Exception {
        byte[] textContent = "This is not an image".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "text.txt", "text/plain", textContent);

        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId())
            .file(file)
            .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadProductImage_ProductNotFound_Returns404() throws Exception {
        byte[] imageContent = new byte[]{(byte) 0xFF, (byte) 0xD8};
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", imageContent);

        mockMvc.perform(multipart("/api/products/{id}/image", 99999L)
            .file(file)
            .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void testUploadProductImage_NoFile_Returns400() throws Exception {
        mockMvc.perform(multipart("/api/products/{id}/image", testProduct.getId())
            .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isBadRequest());
    }

}
