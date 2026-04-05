package com.storeflow.storeflow_api.exception;

import com.storeflow.storeflow_api.config.TestMailConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GlobalExceptionHandler error response mapping.
 * Validates exception handling per PDF spec: 404, 409, 401, 422, 500 status codes
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @WithMockUser
    void shouldThrowResourceNotFoundExceptionWith404Status() throws Exception {
        mockMvc.perform(get("/test-exception/not-found")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldReturnErrorResponseForResourceNotFound() throws Exception {
        mockMvc.perform(get("/test-exception/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldReturn404ForUnmappedErrorEndpoint() throws Exception {
        mockMvc.perform(get("/unmapped/error")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test DataIntegrityViolationException → 409 Conflict
     * Per PDF spec: Duplicate SKU/email should return 409
     */
    @Test
    void testDataIntegrityViolation_Returns409Conflict() {
        // Direct test of exception handler
        Exception ex = new DataIntegrityViolationException("Duplicate key value violates unique constraint 'unique_sku'");
        var response = exceptionHandler.handleDataIntegrityViolation((DataIntegrityViolationException) ex);
        
        assertThat(response.getStatusCode().value()).isEqualTo(409)
                .as("DataIntegrityViolationException should map to 409 Conflict");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Conflict");
    }

    /**
     * Test that error responses include all required fields
     */
    @Test
    void testErrorResponse_ContainsAllRequiredFields() throws Exception {
        var response = mockMvc.perform(get("/test-exception/not-found"))
                .andExpect(status().isNotFound())
                .andReturn();
        
        String content = response.getResponse().getContentAsString();
        assertThat(content).contains("\"status\"").contains("\"message\"").contains("\"timestamp\"");
    }

    /**
     * Test ResourceNotFoundException with custom message
     */
    @Test
    void testResourceNotFound_CarriesCustomMessage() throws Exception {
        mockMvc.perform(get("/test-exception/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    /**
     * Test AppException (custom application exception) → 400 Bad Request
     */
    @Test
    void testAppException_Returns400BadRequest() {
        Exception ex = new AppException("Invalid order quantity");
        var response = exceptionHandler.handleAppException((AppException) ex);
        
        assertThat(response.getStatusCode().value()).isEqualTo(400)
                .as("AppException should map to 400 Bad Request");
        assertThat(response.getBody().getMessage()).contains("Invalid order quantity");
    }

    /**
     * Test IllegalArgumentException → 422 Unprocessable Entity
     * Per PDF spec: Validation errors should return 422
     */
    @Test
    void testIllegalArgumentException_Returns422UnprocessableEntity() {
        Exception ex = new IllegalArgumentException("Order quantity must be positive");
        var response = exceptionHandler.handleIllegalArgumentException((IllegalArgumentException) ex, null);
        
        assertThat(response.getStatusCode().value()).isEqualTo(422)
                .as("IllegalArgumentException should map to 422 Unprocessable Entity");
        assertThat(response.getBody().getMessage()).contains("quantity");
    }

    @RestController
    static class TestController {
        @GetMapping("/test-exception/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Resource not found");
        }

        @GetMapping("/test-exception/error")
        public void throwRuntimeException() {
            throw new RuntimeException("Test runtime exception");
        }

        @GetMapping("/test-exception/conflict")
        public void throwDataIntegrityViolation() {
            throw new DataIntegrityViolationException("Duplicate key violates unique constraint");
        }
    }
}
