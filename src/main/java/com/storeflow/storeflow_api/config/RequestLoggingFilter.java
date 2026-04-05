package com.storeflow.storeflow_api.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Request logging filter that adds MDC (Mapped Diagnostic Context) trace ID to every request.
 * 
 * Functionality:
 * 1. Generates unique trace ID per request (UUID or extracts from X-Trace-Id header)
 * 2. Stores trace ID in SLF4J MDC for automatic inclusion in all logs
 * 3. Adds X-Trace-Id header to response for client-side tracking
 * 4. Logs request method, path, and trace ID at DEBUG level
 * 5. Logs response status code and execution time
 * 6. Cleans up MDC after request completion
 * 
 * Example log output:
 * ```
 * [TRACE-ID: 550e8400-e29b-41d4-a716-446655440000] POST /api/orders - 201 Created (45ms)
 * ```
 * 
 * Usage in application logs:
 * ```java
 * // Automatically includes trace ID
 * logger.info("Processing order");  // Output: [TRACE-ID: 550e8400-...] Processing order
 * ```
 * 
 * @author StoreFlow
 * @version 1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        // Extract or generate trace ID
        String traceId = extractTraceId(request);

        // Store in MDC for automatic log inclusion
        MDC.put(MDC_KEY, traceId);

        // Add to response header
        response.addHeader(TRACE_ID_HEADER, traceId);

        // Log request details
        long startTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Incoming request - {} {} - Trace ID: {}", request.getMethod(), request.getRequestURI(), traceId);
        }

        try {
            // Continue with request processing
            filterChain.doFilter(request, response);
        } finally {
            // Log response details
            long duration = System.currentTimeMillis() - startTime;
            if (log.isDebugEnabled()) {
                log.debug("Request completed - {} {} - Status: {} - Duration: {}ms - Trace ID: {}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration, traceId);
            }

            // Clean up MDC to prevent leaks in thread pools
            MDC.remove(MDC_KEY);
        }
    }

    /**
     * Extract trace ID from request header or generate new one.
     * 
     * Priority:
     * 1. X-Trace-Id header from client (if provided)
     * 2. Generate new UUID v4
     * 
     * @param request HTTP request
     * @return trace ID string
     */
    private String extractTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        
        return traceId;
    }
}
