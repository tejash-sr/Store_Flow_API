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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GlobalExceptionHandler error response mapping.
 * Per Phase 5 specification: validates exception handling and HTTP status codes.
 * Per audit.md: Add unit tests for each exception type in @ControllerAdvice.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test that unknown routes return 404 with error response.
     * Per Phase 5: global error handler returns consistent JSON shape.
     */
    @Test
    @WithMockUser
    void testNotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/nonexistent-path")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test that error responses include required fields.
     * Per audit requirement: verify the error response structure.
     */
    @Test
    @WithMockUser
    void testErrorResponse_ContainsRequiredFields() throws Exception {
        mockMvc.perform(get("/api/nonexistent-path"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").exists());
    }

    /**
     * Test that ResourceNotFoundException returns 404.
     * Per Phase 5: correct HTTP status mapping.
     */
    @Test
    @WithMockUser
    void testResourceNotFound_Returns404Status() throws Exception {
        mockMvc.perform(get("/test-exception/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    /**
     * Test controller that throws exceptions for testing.
     */
    @RestController
    static class TestExceptionController {
        @GetMapping("/test-exception/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Resource not found");
        }
    }
}
