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

    @Test
    void testFindByProductId() {
        orderItemRepository.save(testItem);
        List<OrderItem> items = orderItemRepository.findByProduct_IdOrderByCreatedAtDesc(product.getId());
        assertTrue(items.size() >= 1);
    }

    @Test
    void testCountByOrderId() {
        orderItemRepository.save(testItem);
        Long count = orderItemRepository.countByOrder_Id(order.getId());
        assertTrue(count >= 1);
    }

    @Test
    void testCalculateSubtotal() {
        testItem.setQuantity(10L);
        testItem.setUnitPrice(BigDecimal.valueOf(50.00));

        BigDecimal subtotal = testItem.getSubtotal();
        assertEquals(0, subtotal.compareTo(BigDecimal.valueOf(500.00)));
    }

    @Test
    void testCalculateLineTotal() {
        testItem.setQuantity(10L);
        testItem.setUnitPrice(BigDecimal.valueOf(100.00));
        testItem.setDiscountAmount(BigDecimal.valueOf(50.00));

        BigDecimal lineTotal = testItem.getLineTotal();
        assertEquals(0, lineTotal.compareTo(BigDecimal.valueOf(950.00)));
    }

    @Test
    void testCalculateLineWithoutDiscount() {
        testItem.setQuantity(5L);
        testItem.setUnitPrice(BigDecimal.valueOf(100.00));
        testItem.setDiscountAmount(BigDecimal.ZERO);

        BigDecimal lineTotal = testItem.getLineTotal();
        assertEquals(0, lineTotal.compareTo(BigDecimal.valueOf(500.00)));
    }

    @Test
    void testCalculateDiscountPercentage() {
        testItem.setQuantity(100L);
        testItem.setUnitPrice(BigDecimal.valueOf(100.00));
        testItem.setDiscountAmount(BigDecimal.valueOf(500.00));

        BigDecimal percentage = testItem.getDiscountPercentage();
        assertTrue(percentage.compareTo(BigDecimal.valueOf(5.00)) >= 0);
    }

    @Test
    void testFindItemsWithDiscount() {
        testItem.setDiscountAmount(BigDecimal.valueOf(25.00));
        orderItemRepository.save(testItem);

        List<OrderItem> discountedItems = orderItemRepository.findItemsWithDiscount();
        assertTrue(discountedItems.stream()
            .anyMatch(item -> item.getId().equals(testItem.getId())));
    }

    @Test
    void testMultipleOrderItems() {
        OrderItem item1 = OrderItem.builder()
            .order(order)
            .product(product)
            .quantity(5L)
            .unitPrice(BigDecimal.valueOf(50.00))
            .build();
        OrderItem item2 = OrderItem.builder()
            .order(order)
            .product(product)
            .quantity(3L)
            .unitPrice(BigDecimal.valueOf(75.00))
            .build();

        orderItemRepository.save(item1);
        orderItemRepository.save(item2);

        List<OrderItem> items = orderItemRepository.findByOrder_IdOrderByCreatedAtAsc(order.getId());
        assertEquals(2, items.size());
    }

    @Test
    void testValidateQuantityOnCreate() {
        testItem.setQuantity(-1L);
        assertThrows(Exception.class, () -> {
            orderItemRepository.save(testItem);
        });
    }
}
