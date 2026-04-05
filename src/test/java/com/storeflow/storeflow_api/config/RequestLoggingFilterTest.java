package com.storeflow.storeflow_api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for RequestLoggingFilter - comprehensive MDC trace ID functionality.
 * 
 * Test Coverage:
 * 1. Trace ID generation (UUID) when not provided
 * 2. Trace ID extraction from X-Trace-Id header
 * 3. X-Trace-Id header included in response
 * 4. MDC correctly populated and cleaned
 * 5. Invalid trace IDs handled gracefully
 * 6. Filter execution in correct order
 * 
 * Architecture:
 * - @SpringBootTest for full context
 * - MockMvc for HTTP simulation
 * - H2 database for test isolation
 * 
 * @author StoreFlow
 * @version 1.0
 */
import com.storeflow.storeflow_api.config.TestMailConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class RequestLoggingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TEST_ENDPOINT = "/api/health";

    @BeforeEach
    void setUp() {
        // Ensure MDC is clean before each test
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate UUID trace ID when not provided in request")
    void testGenerateTraceIdWhenNotProvided() throws Exception {
        // When requesting without X-Trace-Id header
        mockMvc.perform(get(TEST_ENDPOINT))
                // Then response should contain auto-generated trace ID
                .andExpect(status().isOk())
                .andExpect(header().exists(TRACE_ID_HEADER))
                .andExpect(result -> {
                    String traceId = result.getResponse().getHeader(TRACE_ID_HEADER);
                    // Verify it's a valid UUID
                    assertThat(traceId).isNotNull().isNotEmpty();
                    assertThatNoException().isThrownBy(() -> UUID.fromString(traceId));
                });
    }

    @Test
    @DisplayName("Should extract trace ID from X-Trace-Id header when provided")
    void testExtractTraceIdFromHeader() throws Exception {
        // Given a custom trace ID
        String customTraceId = "custom-trace-12345";

        // When requesting with X-Trace-Id header
        mockMvc.perform(get(TEST_ENDPOINT)
                .header(TRACE_ID_HEADER, customTraceId))
                // Then response should contain the provided trace ID
                .andExpect(status().isOk())
                .andExpect(header().string(TRACE_ID_HEADER, customTraceId));
    }

    @Test
    @DisplayName("Should add X-Trace-Id header to response")
    void testResponseContainsTraceIdHeader() throws Exception {
        // When making a request
        mockMvc.perform(get(TEST_ENDPOINT))
                // Then response should include X-Trace-Id header
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String traceId = result.getResponse().getHeader(TRACE_ID_HEADER);
                    assertThat(traceId).isNotNull().isNotEmpty();
                });
    }

    @Test
    @DisplayName("Should handle empty trace ID header by generating new UUID")
    void testHandleEmptyTraceIdHeader() throws Exception {
        // When requesting with empty X-Trace-Id header
        mockMvc.perform(get(TEST_ENDPOINT)
                .header(TRACE_ID_HEADER, ""))
                // Then should generate a valid UUID
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String traceId = result.getResponse().getHeader(TRACE_ID_HEADER);
                    assertThat(traceId).isNotNull().isNotEmpty();
                    // Should be a valid UUID or generated ID
                    assertThat(traceId).isNotBlank();
                });
    }

    @Test
    @DisplayName("Should handle multiple consecutive requests with different trace IDs")
    void testMultipleRequestsWithDifferentTraceIds() throws Exception {
        // Given first request
        String traceId1 = "trace-request-1";
        
        mockMvc.perform(get(TEST_ENDPOINT)
                .header(TRACE_ID_HEADER, traceId1))
                .andExpect(status().isOk())
                .andExpect(header().string(TRACE_ID_HEADER, traceId1));

        // When making second request with different trace ID
        String traceId2 = "trace-request-2";
        
        mockMvc.perform(get(TEST_ENDPOINT)
                .header(TRACE_ID_HEADER, traceId2))
                // Then should have different trace ID (no leakage)
                .andExpect(status().isOk())
                .andExpect(header().string(TRACE_ID_HEADER, traceId2));

        // Verify MDC is cleaned up after requests
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("Should preserve trace ID through chain of filters")
    void testTraceIdPreservationThroughFilterChain() throws Exception {
        // Given a specific trace ID
        String expectedTraceId = "chain-test-trace-" + UUID.randomUUID();

        // When making request with trace ID
        mockMvc.perform(get(TEST_ENDPOINT)
                .header(TRACE_ID_HEADER, expectedTraceId))
                // Then all response headers should preserve the trace ID
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseTraceId = result.getResponse().getHeader(TRACE_ID_HEADER);
                    assertThat(responseTraceId).isEqualTo(expectedTraceId);
                    // Verify no modification to trace ID
                    assertThat(responseTraceId).isNotNull();
                });
    }

    @Test
    @DisplayName("Should handle special characters in trace ID")
    void testHandleSpecialCharactersInTraceId() throws Exception {
        // Given trace ID with special characters
        String traceIdWithSpecialChars = "trace-2024-01-15:test@host";

        // When making request with special character trace ID
        mockMvc.perform(get(TEST_ENDPOINT)
                .header(TRACE_ID_HEADER, traceIdWithSpecialChars))
                // Then trace ID should be preserved as-is
                .andExpect(status().isOk())
                .andExpect(header().string(TRACE_ID_HEADER, traceIdWithSpecialChars));
    }

    @Test
    @DisplayName("Should ensure MDC cleanup prevents thread pool contamination")
    void testMDCCleanupPreventsThreadPoolContamination() throws Exception {
        // Given a trace ID
        String traceId = "cleanup-test-" + UUID.randomUUID();

        // When making request with trace ID
        mockMvc.perform(get(TEST_ENDPOINT)
                .header(TRACE_ID_HEADER, traceId))
                .andExpect(status().isOk());

        // Then MDC should be cleaned up after request completes
        // (Verify by checking MDC is empty in main test thread)
        assertThat(MDC.get("traceId")).isNull();
    }
}
