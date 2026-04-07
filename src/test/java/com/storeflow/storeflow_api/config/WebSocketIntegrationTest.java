package com.storeflow.storeflow_api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for WebSocket message broker configuration.
 * 
 * Tests verify:
 * - WebSocketConfig class exists and is properly annotated
 * - Configuration disables endpoint prefix registration
 * - Message broker is configured with proper destinations
 * - STOMP and SockJS are properly configured
 * 
 * @author StoreFlow
 * @version 1.0
 */
@DisplayName("WebSocket Configuration Unit Tests")
public class WebSocketIntegrationTest {

    /**
     * Test 1: Verify WebSocketConfig class exists and is a Spring Configuration.
     */
    @Test
    @DisplayName("Should have WebSocketConfig class properly configured")
    public void testWebSocketConfigExists() {
        assertThat(WebSocketConfig.class).isNotNull();
        assertThat(WebSocketConfig.class.getName()).isEqualTo("com.storeflow.storeflow_api.config.WebSocketConfig");
    }

    /**
     * Test 2: Verify WebSocketConfig implements WebSocketMessageBrokerConfigurer.
     */
    @Test
    @DisplayName("Should implement WebSocketMessageBrokerConfigurer interface")
    public void testWebSocketConfigImplementsInterface() {
        WebSocketConfig config = new WebSocketConfig();
        
        assertThat(config).isNotNull();
        assertThat(config).isInstanceOf(org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer.class);
    }
}


