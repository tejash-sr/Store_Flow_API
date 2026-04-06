package com.storeflow.storeflow_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.storeflow.storeflow_api.dto.OrderRequest;
import com.storeflow.storeflow_api.dto.OrderResponse;
import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.service.OrderService;
import com.storeflow.storeflow_api.service.PdfGenerationService;
import com.storeflow.storeflow_api.service.CsvExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Order endpoints.
 * Implements order placement, listing, and status management.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management endpoints - place orders, list orders, update status, generate reports")
public class OrderController {

    private final OrderService orderService;
    private final PdfGenerationService pdfGenerationService;
    private final CsvExportService csvExportService;

    /**
     * POST /api/orders - Place a new order (atomic transaction).
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest request) {
        try {
            OrderResponse response = orderService.placeOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        }
    }

    /**
     * GET /api/orders - List all orders with pagination (USER: own, ADMIN: all).
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(Pageable pageable) {
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/{id} - Get order details with all order items.
     */
    @PreAuthorize("@orderOwnershipChecker.isOwner(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Optional<OrderResponse> order = orderService.getOrderById(id);
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("{ \"error\": \"Order not found\" }");
    }

    /**
     * PATCH /api/orders/{id}/status - Update order status (admin, valid transitions only).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            OrderResponse response = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        }
    }

    /**
     * GET /api/orders/{id}/report - Generate PDF report for an order.
     */
    @PreAuthorize("@orderOwnershipChecker.isOwner(#id)")
    @GetMapping("/{id}/report")
    public ResponseEntity<?> getOrderReport(@PathVariable Long id) {
        try {
            Optional<OrderResponse> orderOpt = orderService.getOrderById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{ \"error\": \"Order not found\" }");
            }

            // Fetch full Order entity (with items)
            Order order = orderService.getOrderEntityById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            // Generate PDF
            byte[] pdfBytes = pdfGenerationService.generateOrderReport(order);
            log.info("PDF report generated for order {}", id);

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"order-" + order.getOrderNumber() + ".pdf\"")
                .body(new ByteArrayResource(pdfBytes));

        } catch (IOException e) {
            log.error("Failed to generate PDF for order {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{ \"error\": \"Failed to generate PDF\" }");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{ \"error\": \"" + e.getMessage() + "\" }");
        }
    }

    /**
     * GET /api/orders/export - Export orders as CSV (optionally filtered by date range).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<?> exportOrdersAsCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        try {
            List<Order> orders = orderService.getAllOrdersForExport();

            byte[] csvBytes;
            if (from != null && to != null) {
                csvBytes = csvExportService.generateOrdersCsvWithDateFilter(orders, from, to);
            } else {
                csvBytes = csvExportService.generateOrdersCsv(orders);
            }

            log.info("CSV export generated for {} orders", orders.size());

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orders-export.csv\"")
                .body(new ByteArrayResource(csvBytes));

        } catch (IOException e) {
            log.error("Failed to generate CSV export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{ \"error\": \"Failed to generate CSV export\" }");
        }
    }
}
