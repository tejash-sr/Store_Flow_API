package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HealthControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldReturnHealthStatusWithCorrectShape() throws Exception {
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.uptimeMs").isNumber())
                .andExpect(jsonPath("$.uptimeMs", greaterThanOrEqualTo(0)));
    }

    @Test
    void shouldReturnOkWithValidResponse() throws Exception {
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", hasKey("status")))
                .andExpect(jsonPath("$", hasKey("timestamp")))
                .andExpect(jsonPath("$", hasKey("uptimeMs")));
    }

    @Test
    void shouldReturnHealthStatusUpIndicatingApplicationIsRunning() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
