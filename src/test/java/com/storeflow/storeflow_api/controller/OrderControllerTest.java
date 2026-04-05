package com.storeflow.storeflow_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storeflow.storeflow_api.config.TestMailConfig;
import com.storeflow.storeflow_api.dto.OrderItemRequest;
import com.storeflow.storeflow_api.dto.OrderRequest;
import com.storeflow.storeflow_api.entity.Category;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OrderController REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Product testProduct;
    private Store testStore;

    @BeforeEach
    void setUp() {
        // Create test store
        testStore = storeRepository.save(Store.builder()
            .storeCode("STORE001")
            .name("Main Store")
            .address("123 Main St")
            .phoneNumber("555-0001")
            .email("store@example.com")
            .city("Springfield")
            .state("IL")
            .isActive(true)
            .build());

        // Create test category
        Category category = categoryRepository.save(Category.builder()
            .name("Electronics")
            .isActive(true)
            .build());

        // Create test product
        testProduct = productRepository.save(Product.builder()
            .name("Test Product")
            .sku("TST001")
            .price(BigDecimal.valueOf(99.99))
            .stockQuantity(10L)
            .category(category)
            .isActive(true)
            .build());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testPlaceOrderSuccess() throws Exception {
        OrderRequest request = OrderRequest.builder()
            .customerId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(testProduct.getId())
                    .quantity(2L)
                    .build()
            ))
            .build();

        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.totalAmount").value(199.98));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testPlaceOrderEmptyItems() throws Exception {
        OrderRequest request = OrderRequest.builder()
            .customerId(1L)
            .items(List.of())
            .build();

        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testPlaceOrderProductNotFound() throws Exception {
        OrderRequest request = OrderRequest.builder()
            .customerId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(99999L)
                    .quantity(1L)
                    .build()
            ))
            .build();

        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetAllOrders() throws Exception {
        mockMvc.perform(get("/api/orders")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetOrderByIdSuccess() throws Exception {
        // Create an order first
        OrderRequest request = OrderRequest.builder()
            .customerId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(testProduct.getId())
                    .quantity(1L)
                    .build()
            ))
            .build();

        // Place the order
        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Get the created order
        var createdOrder = orderRepository.findAll().stream().findFirst();
        assert createdOrder.isPresent() : "Order should have been created";

        Long orderId = createdOrder.get().getId();
        mockMvc.perform(get("/api/orders/" + orderId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetOrderByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/99999")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testUpdateOrders() throws Exception {
        // Create an order first
        OrderRequest request = OrderRequest.builder()
            .customerId(1L)
            .items(List.of(
                OrderItemRequest.builder()
                    .productId(testProduct.getId())
                    .quantity(1L)
                    .build()
            ))
            .build();

        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Get the created order
        var orders = orderRepository.findAll();
        assert !orders.isEmpty() : "Order should have been created";

        Long orderId = orders.get(0).getId();
        mockMvc.perform(patch("/api/orders/" + orderId + "/status?status=PROCESSING")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PROCESSING"));
    }
}
