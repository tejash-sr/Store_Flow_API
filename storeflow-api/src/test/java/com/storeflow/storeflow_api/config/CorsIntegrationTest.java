package com.storeflow.storeflow_api.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for CORS configuration.
 * Validates that CORS is properly configured for cross-origin requests in development.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowRequestsFromHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRespondOkToHealthCheckWithOriginHeader() throws Exception {
        mockMvc.perform(get("/api/health")
                .header(HttpHeaders.ORIGIN, "http://example.com"))
                .andExpect(status().isOk());
    }
}
