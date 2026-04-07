package com.storeflow.storeflow_api.repository;

import com.storeflow.AbstractRepositoryTest;
import com.storeflow.storeflow_api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OrderItemRepository.
 * 
 * Uses AbstractRepositoryTest which provides isolated PostgreSQL container.
 */
@Transactional
class OrderItemRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Order order;
    private Product product;
    private OrderItem testItem;

    @BeforeEach
    public void setUp() {
        Store store = storeRepository.save(Store.builder()
            .storeCode("TEST-001")
            .name("Test Store")
            .address("123 Main St")
            .isActive(true)
            .build());

        Category cat = categoryRepository.save(Category.builder()
            .name("Test Category")
            .isActive(true)
            .build());

        product = productRepository.save(Product.builder()
            .sku("TEST-SKU")
            .name("Test Product")
            .price(BigDecimal.valueOf(99.99))
            .category(cat)
            .isActive(true)
            .build());

        order = orderRepository.save(Order.builder()
            .orderNumber("ORD-TEST-001")
            .store(store)
            .customerName("Test Customer")
            .status(Order.OrderStatus.PENDING)
            .total(BigDecimal.ZERO)
            .build());

        testItem = OrderItem.builder()
            .order(order)
            .product(product)
            .quantity(5L)
            .unitPrice(BigDecimal.valueOf(99.99))
            .discountAmount(BigDecimal.ZERO)
            .build();
    }

    @Test
    void testSaveOrderItemAndRetrieve() {
        OrderItem saved = orderItemRepository.save(testItem);
        assertNotNull(saved.getId());
        assertEquals(5L, saved.getQuantity());
    }

    @Test
    void testFindByOrderId() {
        orderItemRepository.save(testItem);
        List<OrderItem> items = orderItemRepository.findByOrder_IdOrderByCreatedAtAsc(order.getId());
        assertEquals(1, items.size());
        assertEquals(5L, items.get(0).getQuantity());
    }


}
