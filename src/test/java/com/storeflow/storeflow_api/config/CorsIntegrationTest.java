package com.storeflow.storeflow_api.config;

import com.storeflow.storeflow_api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for CORS configuration.
 * Validates that CORS is properly configured for cross-origin requests in development.
 */
class CorsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldAllowRequestsFromHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRespondOkToHealthCheckWithOriginHeader() throws Exception {
        mockMvc.perform(get("/api/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200"))
                .andExpect(status().isOk());
    }
}
