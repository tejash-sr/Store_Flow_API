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
 * Validates exception handling through Spring's exception translation layer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

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
    }
}
