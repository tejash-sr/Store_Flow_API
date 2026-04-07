package com.storeflow.storeflow_api.repository;

import com.storeflow.AbstractRepositoryTest;
import com.storeflow.storeflow_api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OrderRepository.
 * 
 * Uses AbstractRepositoryTest which provides isolated PostgreSQL container.
 */
@Transactional
class OrderRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Store store;
    private Order testOrder;

    @BeforeEach
    public void setUp() {
        store = storeRepository.save(Store.builder()
            .storeCode("TEST-STORE")
            .name("Test Store")
            .address("123 Main St")
            .isActive(true)
            .build());

        ShippingAddress address = ShippingAddress.builder()
            .street("456 Oak St")
            .city("Springfield")
            .state("IL")
            .postalCode("62701")
            .country("USA")
            .build();

        testOrder = Order.builder()
            .orderNumber("ORD-001")
            .customerName("John Doe")
            .customerEmail("john@example.com")
            .customerPhone("5551234567")
            .shippingAddress(address)
            .status(Order.OrderStatus.PENDING)
            .subtotal(BigDecimal.valueOf(100.00))
            .tax(BigDecimal.valueOf(8.00))
            .total(BigDecimal.valueOf(108.00))
            .build();
    }

    @Test
    void testSaveOrderAndRetrieve() {
        Order saved = orderRepository.save(testOrder);
        assertNotNull(saved.getId());
        assertEquals("ORD-001", saved.getOrderNumber());
    }

    @Test
    void testFindByOrderNumber() {
        orderRepository.save(testOrder);
        Optional<Order> found = orderRepository.findByOrderNumber("ORD-001");
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getCustomerName());
    }


}
