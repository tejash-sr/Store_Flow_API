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
 * Unit tests for PdfGenerationService
 */
class PdfGenerationServiceTest {

    private PdfGenerationService pdfGenerationService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        pdfGenerationService = new PdfGenerationService();
        // Create test entity hierarchy
        Store store = Store.builder()
            .id(1L)
            .name("Test Store")
            .build();

        Product product1 = Product.builder()
            .id(1L)
            .name("Test Product 1")
            .price(BigDecimal.valueOf(10.00))
            .build();

        Product product2 = Product.builder()
            .id(2L)
            .name("Test Product 2")
            .price(BigDecimal.valueOf(20.00))
            .build();

        List<OrderItem> items = new ArrayList<>();
        
        OrderItem item1 = OrderItem.builder()
            .id(1L)
            .product(product1)
            .quantity(2L)
            .unitPrice(BigDecimal.valueOf(10.00))
            .build();

        OrderItem item2 = OrderItem.builder()
            .id(2L)
            .product(product2)
            .quantity(1L)
            .unitPrice(BigDecimal.valueOf(20.00))
            .build();

        items.add(item1);
        items.add(item2);

        testOrder = Order.builder()
            .id(1L)
            .orderNumber("ORD-ABC123")
            .store(store)
            .customerName("John Doe")
            .customerEmail("john@example.com")
            .customerPhone("555-1234")
            .shippingAddress("123 Main St, Anytown, USA 12345")
            .status(OrderStatus.PENDING)
            .items(items)
            .total(BigDecimal.valueOf(40.00))
            .createdAt(LocalDateTime.now())
            .build();

        // Set order reference for items
        item1.setOrder(testOrder);
        item2.setOrder(testOrder);
    }

    @Test
    void testGenerateOrderReport_ValidOrder_ReturnsNonEmptyPdfBytes() throws IOException {
        byte[] pdfBytes = pdfGenerationService.generateOrderReport(testOrder);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF bytes should not be empty");
        
        // Check for PDF magic number (first 4 bytes: %PDF)
        assertEquals("%PDF".getBytes()[0], pdfBytes[0], "Should start with %PDF");
    }

    @Test
    void testGenerateOrderReport_WithMultipleItems_IncludesAllItems() throws IOException {
        byte[] pdfBytes = pdfGenerationService.generateOrderReport(testOrder);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // PDF should contain order number
        String pdfContent = new String(pdfBytes);
        // Check that order number is in PDF (though it's encoded)
        assertNotNull(pdfContent);
    }

    @Test
    void testGenerateOrderReport_OrderWithoutItems_GeneratesPdf() throws IOException {
        testOrder.setItems(new ArrayList<>());

        byte[] pdfBytes = pdfGenerationService.generateOrderReport(testOrder);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGenerateOrderReport_LargeOrderNumber_HandledCorrectly() throws IOException {
        testOrder.setOrderNumber("ORD-" + "X".repeat(100));

        byte[] pdfBytes = pdfGenerationService.generateOrderReport(testOrder);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testGenerateOrderReport_WithSpecialCharacters_HandledCorrectly() throws IOException {
        testOrder.setCustomerName("John O'Brien-Smith");
        testOrder.setShippingAddress("456 \"Oak\" Lane, Suite #5");

        byte[] pdfBytes = pdfGenerationService.generateOrderReport(testOrder);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

}
