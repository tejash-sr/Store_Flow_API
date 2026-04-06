package com.storeflow.storeflow_api.exception;

import com.storeflow.storeflow_api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for 404 error handling on non-existent routes.
 */
class ErrorHandlingIntegrationTest extends AbstractIntegrationTest {

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
