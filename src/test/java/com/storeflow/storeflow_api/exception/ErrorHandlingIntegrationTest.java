package com.storeflow.storeflow_api.exception;

import com.storeflow.storeflow_api.config.TestMailConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.storeflow.storeflow_api.testsupport.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for 404 error handling on non-existent routes.
 * Validates that unmatched routes return appropriate HTTP 404 status.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class ErrorHandlingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldReturn404ForNonexistentApiRoute() throws Exception {
        mockMvc.perform(get("/api/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldReturn404StatusCodeForInvalidPath() throws Exception {
        mockMvc.perform(get("/invalid/path")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldConsistentlyReturn404ForUnmatchedRoutes() throws Exception {
        mockMvc.perform(get("/some/random/unmapped/endpoint"))
                .andExpect(status().isNotFound());
    }
}
