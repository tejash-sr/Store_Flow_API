package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.AbstractRepositoryTest;
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

    @Test
    void testFindByOrderNumberNotFound() {
        Optional<Order> found = orderRepository.findByOrderNumber("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByStore() {
        orderRepository.save(testOrder);
        List<Order> orders = orderRepository.findByStore_IdOrderByCreatedAtDesc(store.getId());
        assertTrue(orders.stream().anyMatch(o -> "ORD-001".equals(o.getOrderNumber())));
    }

    @Test
    void testFindByStatus() {
        orderRepository.save(testOrder);
        Order shipped = Order.builder()
            .orderNumber("ORD-002")
            .customerName("Jane Doe")
            .status(Order.OrderStatus.SHIPPED)
            .total(BigDecimal.valueOf(200.00))
            .build();
        orderRepository.save(shipped);

        List<Order> pending = orderRepository.findByStatusOrderByCreatedAtAsc(Order.OrderStatus.PENDING);
        assertTrue(pending.stream().anyMatch(o -> "ORD-001".equals(o.getOrderNumber())));
    }

    @Test
    void testCountByStatus() {
        orderRepository.save(testOrder);
        orderRepository.save(Order.builder()
            .orderNumber("ORD-003")
            .status(Order.OrderStatus.PENDING)
            .total(BigDecimal.ZERO)
            .build());

        Long count = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        assertTrue(count >= 2);
    }

    @Test
    void testCountByStore() {
        orderRepository.save(testOrder);
        Long count = orderRepository.countByStore_Id(store.getId());
        assertTrue(count >= 1);
    }

    @Test
    void testFindByCustomerEmail() {
        orderRepository.save(testOrder);
        List<Order> orders = orderRepository.findByCustomerEmailOrderByCreatedAtDesc("john@example.com");
        assertTrue(orders.stream().anyMatch(o -> "ORD-001".equals(o.getOrderNumber())));
    }

    @Test
    void testOrderStatusTransitions() {
        Order saved = orderRepository.save(testOrder);
        assertEquals(Order.OrderStatus.PENDING, saved.getStatus());

        saved.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(saved);

        Optional<Order> updated = orderRepository.findByOrderNumber("ORD-001");
        assertTrue(updated.isPresent());
        assertEquals(Order.OrderStatus.CONFIRMED, updated.get().getStatus());
    }

    @Test
    void testCanBeCancelled() {
        assertTrue(testOrder.canBeCancelled());

        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        assertFalse(testOrder.canBeCancelled());
    }
}
