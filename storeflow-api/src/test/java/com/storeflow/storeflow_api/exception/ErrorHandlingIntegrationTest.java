package com.storeflow.storeflow_api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn404ForNonexistentRoute() throws Exception {
        mockMvc.perform(get("/api/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnConsistentErrorResponseShape() throws Exception {
        mockMvc.perform(get("/invalid/path")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").isNumber())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void shouldHavePathInErrorResponse() throws Exception {
        mockMvc.perform(get("/some/invalid/endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").exists());
    }
}
