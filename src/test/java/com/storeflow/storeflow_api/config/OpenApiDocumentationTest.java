package com.storeflow.storeflow_api.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.storeflow.storeflow_api.testsupport.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OpenAPI/Swagger Documentation Tests
 * Verifies that:
 * - Swagger UI is accessible at /swagger-ui.html
 * - OpenAPI JSON spec is available at /v3/api-docs
 * - Bearer token security scheme is defined
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class OpenApiDocumentationTest extends AbstractIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testSwaggerUiIsAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }
    
    @Test
    void testSwaggerUiResourcesAreAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
    
    @Test
    void testOpenApiJsonSchemaIsAvailable() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();
        
        String jsonContent = result.getResponse().getContentAsString();
        
        // Verify essential OpenAPI schema components
        assertThat(jsonContent).contains("\"openapi\":\"3");
        assertThat(jsonContent).contains("\"title\":\"StoreFlow API\"");
        assertThat(jsonContent).contains("\"version\":\"0.8.0\"");
        assertThat(jsonContent).contains("bearer-jwt");
    }
    
    @Test
    void testOpenApiSchemaContainsBearerSecurityScheme() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();
        
        String jsonContent = result.getResponse().getContentAsString();
        
        // Verify JWT security scheme is defined
        assertThat(jsonContent)
                .contains("\"type\":\"http\"")
                .contains("\"scheme\":\"bearer\"")
                .contains("\"bearerFormat\":\"JWT\"");
    }
    
    @Test
    void testOpenApiSchemaContainsApiInfo() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();
        
        String jsonContent = result.getResponse().getContentAsString();
        
        // Verify API info is complete
        assertThat(jsonContent)
                .contains("\"title\":\"StoreFlow API\"")
                .contains("\"description\"")
                .contains("\"version\":\"0.8.0\"")
                .contains("\"contact\"")
                .contains("\"license\"");
    }
    
    @Test
    void testOpenApiYamlFormatIsAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith("application/vnd.oai.openapi"));
    }
}
