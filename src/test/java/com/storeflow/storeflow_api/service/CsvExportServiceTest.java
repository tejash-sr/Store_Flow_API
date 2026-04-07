package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.entity.Order.OrderStatus;
import com.storeflow.storeflow_api.entity.OrderItem;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CsvExportService
 */
class CsvExportServiceTest {

    private CsvExportService csvExportService;

    private List<Order> testOrders;

    @BeforeEach
    void setUp() {
        csvExportService = new CsvExportService();
        testOrders = new ArrayList<>();

        Store store = Store.builder()
            .id(1L)
            .name("Test Store")
            .build();

        Product product1 = Product.builder()
            .id(1L)
            .name("Product A")
            .price(BigDecimal.valueOf(10.00))
            .build();

        Product product2 = Product.builder()
            .id(2L)
            .name("Product B")
            .price(BigDecimal.valueOf(20.00))
            .build();

        // Create first order
        List<OrderItem> items1 = new ArrayList<>();
        OrderItem item1 = OrderItem.builder()
            .id(1L)
            .product(product1)
            .quantity(2L)
            .unitPrice(BigDecimal.valueOf(10.00))
            .build();
        items1.add(item1);

        Order order1 = Order.builder()
            .id(1L)
            .orderNumber("ORD-001")
            .customerName("John Doe")
            .customerEmail("john@example.com")
            .status(OrderStatus.PENDING)
            .items(items1)
            .total(BigDecimal.valueOf(20.00))
            .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
            .build();
        item1.setOrder(order1);

        // Create second order
        List<OrderItem> items2 = new ArrayList<>();
        OrderItem item2 = OrderItem.builder()
            .id(2L)
            .product(product2)
            .quantity(1L)
            .unitPrice(BigDecimal.valueOf(20.00))
            .build();
        items2.add(item2);

        Order order2 = Order.builder()
            .id(2L)
            .orderNumber("ORD-002")
            .customerName("Jane Smith")
            .customerEmail("jane@example.com")
            .status(OrderStatus.CONFIRMED)
            .items(items2)
            .total(BigDecimal.valueOf(20.00))
            .createdAt(LocalDateTime.of(2024, 1, 20, 14, 30))
            .build();
        item2.setOrder(order2);

        testOrders.add(order1);
        testOrders.add(order2);
    }

    @Test
    void testGenerateOrdersCsv_ValidOrders_ReturnsNonEmptyBytes() throws IOException {
        byte[] csvBytes = csvExportService.generateOrdersCsv(testOrders);

        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 0, "CSV bytes should not be empty");
    }

    @Test
    void testGenerateOrdersCsv_ContainsHeaderRow() throws IOException {
        byte[] csvBytes = csvExportService.generateOrdersCsv(testOrders);
        String csvContent = new String(csvBytes);

        // Should contain header row
        assertTrue(csvContent.contains("Order Number"));
        assertTrue(csvContent.contains("Customer Name"));
        assertTrue(csvContent.contains("Product Name"));
        assertTrue(csvContent.contains("Quantity"));
    }

    @Test
    void testGenerateOrdersCsv_ContainsOrderData() throws IOException {
        byte[] csvBytes = csvExportService.generateOrdersCsv(testOrders);
        String csvContent = new String(csvBytes);

        // Should contain first order data
        assertTrue(csvContent.contains("ORD-001"));
        assertTrue(csvContent.contains("John Doe"));
        assertTrue(csvContent.contains("Product A"));

        // Should contain second order data
        assertTrue(csvContent.contains("ORD-002"));
        assertTrue(csvContent.contains("Jane Smith"));
        assertTrue(csvContent.contains("Product B"));
    }

    @Test
    void testGenerateOrdersCsv_MultipleItemsPerOrder_EachRowPerItem() throws IOException {
        Order order = testOrders.get(0);
        Product product = Product.builder()
            .id(3L)
            .name("Product C")
            .price(BigDecimal.valueOf(15.00))
            .build();

        OrderItem item = OrderItem.builder()
            .id(3L)
            .product(product)
            .quantity(1L)
            .unitPrice(BigDecimal.valueOf(15.00))
            .build();
        item.setOrder(order);
        order.getItems().add(item);

        byte[] csvBytes = csvExportService.generateOrdersCsv(List.of(order));
        String csvContent = new String(csvBytes);

        // Should have 2 data rows for 2 items
        long rowCount = csvContent.split("\n").length - 1; // Minus header
        assertTrue(rowCount >= 2, "Should have at least 2 data rows");
    }

    @Test
    void testGenerateOrdersCsv_EmptyOrderList_ReturnsOnlyHeader() throws IOException {
        byte[] csvBytes = csvExportService.generateOrdersCsv(new ArrayList<>());
        String csvContent = new String(csvBytes);

        // Should contain only header
        assertTrue(csvContent.contains("Order Number"));
        assertEquals(1, csvContent.split("\n").length, "Should have only header row");
    }

    @Test
    void testGenerateOrdersCsvWithDateFilter_FilteredRange_ReturnsOrdersInRange() throws IOException {
        LocalDateTime from = LocalDateTime.of(2024, 1, 10, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 18, 23, 59);

        byte[] csvBytes = csvExportService.generateOrdersCsvWithDateFilter(testOrders, from, to);
        String csvContent = new String(csvBytes);

        // Should contain first order (2024-01-15)
        assertTrue(csvContent.contains("ORD-001"));

        // Should not contain second order (2024-01-20)
        assertFalse(csvContent.contains("ORD-002"));
    }

    @Test
    void testGenerateOrdersCsvWithDateFilter_AllOrdersOutOfRange_ReturnsOnlyHeader() throws IOException {
        LocalDateTime from = LocalDateTime.of(2024, 2, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 2, 28, 23, 59);

        byte[] csvBytes = csvExportService.generateOrdersCsvWithDateFilter(testOrders, from, to);
        String csvContent = new String(csvBytes);

        // Should contain only header
        assertTrue(csvContent.contains("Order Number"));
        assertEquals(1, csvContent.split("\n").length, "Should have only header row");
    }

    @Test
    void testGenerateOrdersCsvWithDateFilter_InclusiveBoundaries_IncludesEdgeDates() throws IOException {
        LocalDateTime from = LocalDateTime.of(2024, 1, 15, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 20, 23, 59);

        byte[] csvBytes = csvExportService.generateOrdersCsvWithDateFilter(testOrders, from, to);
        String csvContent = new String(csvBytes);

        // Both orders should be included (boundaries are inclusive)
        assertTrue(csvContent.contains("ORD-001"));
        assertTrue(csvContent.contains("ORD-002"));
    }

}
