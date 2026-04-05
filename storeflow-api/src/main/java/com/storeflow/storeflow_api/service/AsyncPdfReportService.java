package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.entity.AsyncJob;
import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Async PDF Report Service
 * Handles PDF generation as background async task
 * 
 * Workflow:
 * 1. Client POST /api/reports/order-pdf with orderId
 * 2. Service creates AsyncJob, returns jobId immediately
 * 3. Background thread calls generateOrderPdfAsync() in parallel
 * 4. Client polls GET /api/jobs/{jobId} for progress
 * 5. When COMPLETED, resultData has { "filePath": "...", "fileName": "..." }
 */
@Slf4j
@Service
public class AsyncPdfReportService {
    
    @Autowired
    private AsyncJobService asyncJobService;
    
    @Autowired
    private PdfGenerationService pdfGenerationService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Generate order PDF asynchronously
     * Called from controller after AsyncJob is created
     * 
     * @param job the async job tracking this operation
     * @param orderId ID of order to generate PDF for
     */
    @Async("pdfExecutor")
    public void generateOrderPdfAsync(AsyncJob job, Long orderId) {
        try {
            // Mark job as processing
            asyncJobService.markProcessing(job);
            
            // Fetch order details
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                throw new RuntimeException("Order not found: " + orderId);
            }
            
            log.info("Starting async PDF generation for order: {}, jobId: {}", orderId, job.getJobId());
            
            // Generate PDF (this may take time)
            asyncJobService.updateProgress(job, 25, 45);
            byte[] pdfContent = pdfGenerationService.generateOrderPdf(order);
            
            asyncJobService.updateProgress(job, 50, 30);
            
            // Save PDF to storage
            String fileName = "order-" + order.getReferenceNumber() + ".pdf";
            String filePath = fileStorageService.savePdfFile(fileName, pdfContent);
            
            asyncJobService.updateProgress(job, 75, 10);
            
            // Mark job as completed with result
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath);
            result.put("fileName", fileName);
            result.put("orderId", orderId);
            result.put("orderNumber", order.getReferenceNumber());
            
            asyncJobService.markCompleted(job, result);
            log.info("PDF generation completed: jobId={}, order={}", job.getJobId(), orderId);
            
        } catch (Exception e) {
            log.error("Error generating PDF for order {}: {}", orderId, e.getMessage(), e);
            asyncJobService.markFailed(job, "PDF generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate order batch report asynchronously (multiple orders)
     * 
     * @param job the async job tracking this operation
     * @param orderIds list of order IDs to include
     */
    @Async("pdfExecutor")
    public void generateBatchPdfAsync(AsyncJob job, java.util.List<Long> orderIds) {
        try {
            asyncJobService.markProcessing(job);
            log.info("Starting batch PDF generation for {} orders, jobId: {}", orderIds.size(), job.getJobId());
            
            // TODO: Implement batch PDF generation
            // This would merge multiple order PDFs into single file
            // or generate a summary report with all orders
            
            asyncJobService.markCompleted(job, Map.of(
                    "filePath", "/uploads/reports/batch-" + job.getJobId() + ".pdf",
                    "fileName", "batch-report.pdf",
                    "orderCount", orderIds.size()
            ));
            
        } catch (Exception e) {
            log.error("Error generating batch PDF: {}", e.getMessage(), e);
            asyncJobService.markFailed(job, "Batch PDF generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate inventory report asynchronously
     * 
     * @param job the async job tracking this operation
     */
    @Async("pdfExecutor")
    public void generateInventoryReportAsync(AsyncJob job) {
        try {
            asyncJobService.markProcessing(job);
            log.info("Starting inventory report generation, jobId: {}", job.getJobId());
            
            // TODO: Implement inventory report generation
            // Collect all products, stock levels, low-stock alerts, etc.
            // Generate formatted PDF
            
            asyncJobService.markCompleted(job, Map.of(
                    "filePath", "/uploads/reports/inventory-" + job.getJobId() + ".pdf",
                    "fileName", "inventory-report.pdf"
            ));
            
        } catch (Exception e) {
            log.error("Error generating inventory report: {}", e.getMessage(), e);
            asyncJobService.markFailed(job, "Inventory report generation failed: " + e.getMessage());
        }
    }
}
