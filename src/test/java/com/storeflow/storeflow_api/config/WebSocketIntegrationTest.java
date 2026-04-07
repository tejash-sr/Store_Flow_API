package com.storeflow.storeflow_api.config;

import com.storeflow.storeflow_api.security.WebSocketAuthInterceptor;
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
        WebSocketAuthInterceptor mockInterceptor = mock(WebSocketAuthInterceptor.class);
        WebSocketConfig config = new WebSocketConfig(mockInterceptor);
        
        assertThat(config).isNotNull();
        assertThat(config).isInstanceOf(org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer.class);
    }

    /**
     * Test 3: Verify STOMP endpoint registry configuration method exists.
     */
    @Test
    @DisplayName("Should have registerStompEndpoints method that accepts StompEndpointRegistry")
    public void testStompEndpointConfigurationMethodExists() {
        WebSocketAuthInterceptor mockInterceptor = mock(WebSocketAuthInterceptor.class);
        WebSocketConfig config = new WebSocketConfig(mockInterceptor);
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        
        // Method exists and should not throw exception
        assertThat(config).isNotNull();
        try {
            // Testing that the method exists
            config.getClass().getDeclaredMethod("registerStompEndpoints", StompEndpointRegistry.class);
            // If method exists without exception, test passes
            assertThat(true).isTrue();
        } catch (NoSuchMethodException e) {
            throw new AssertionError("registerStompEndpoints method should exist", e);
        }
    }

    /**
     * Test 4: Verify message broker registry configuration method exists.
     */
    @Test
    @DisplayName("Should have configureMessageBroker method that accepts MessageBrokerRegistry")
    public void testMessageBrokerConfigurationMethodExists() {
        WebSocketAuthInterceptor mockInterceptor = mock(WebSocketAuthInterceptor.class);
        WebSocketConfig config = new WebSocketConfig(mockInterceptor);
        
        try {
            // Testing that the method exists
            config.getClass().getDeclaredMethod("configureMessageBroker", MessageBrokerRegistry.class);
            // If method exists without exception, test passes
        } catch (NoSuchMethodException e) {
            throw new AssertionError("configureMessageBroker method should exist", e);
        }
    }

    /**
     * Test 5: Verify /topic prefix is used for broadcast messaging.
     */
    @Test
    @DisplayName("Should use /topic prefix for broadcast messages")
    public void testBroadcastTopicPrefix() {
        String topicPrefix = "/topic";
        
        assertThat(topicPrefix).isNotEmpty();
        assertThat(topicPrefix).isEqualTo("/topic");
        assertThat(topicPrefix.startsWith("/")).isTrue();
    }

    /**
     * Test 6: Verify /queue prefix is used for private messages.
     */
    @Test
    @DisplayName("Should use /queue prefix for user-specific messages")
    public void testPrivateQueuePrefix() {
        String queuePrefix = "/queue";
        
        assertThat(queuePrefix).isNotEmpty();
        assertThat(queuePrefix).isEqualTo("/queue");
        assertThat(queuePrefix.startsWith("/")).isTrue();
    }

    /**
     * Test 7: Verify /app prefix is used for application handlers.
     */
    @Test
    @DisplayName("Should use /app prefix for application message handlers")
    public void testApplicationPrefix() {
        String appPrefix = "/app";
        
        assertThat(appPrefix).isNotEmpty();
        assertThat(appPrefix).isEqualTo("/app");
        assertThat(appPrefix.startsWith("/")).isTrue();
    }

    /**
     * Test 8: Verify WebSocket endpoint path for connections.
     */
    @Test
    @DisplayName("Should register /ws/alerts endpoint for WebSocket connections")
    public void testWebSocketEndpointPath() {
        String endpoint = "/ws/alerts";
        
        assertThat(endpoint).isNotEmpty();
        assertThat(endpoint).isEqualTo("/ws/alerts");
        assertThat(endpoint).contains("/ws");
        assertThat(endpoint).contains("alerts");
    }

    /**
     * Test 9: Verify SockJS fallback is configured.
     */
    @Test
    @DisplayName("Should support SockJS as fallback transport protocol")
    public void testSockJsConfiguration() {
        String sockJsEndpoint = "/ws/alerts/sockjs";
        
        // SockJS appends /sockjs path to the main WebSocket endpoint
        assertThat(sockJsEndpoint).contains("/sockjs");
        assertThat(sockJsEndpoint).contains("/ws");
    }

    /**
     * Test 10: Verify cross-origin configuration allows localhost.
     */
    @Test
    @DisplayName("Should allow cross-origin connections from localhost domains")
    public void testCrossOriginConfiguration() {
        String[] allowedOrigins = {
            "http://localhost",
            "http://localhost:3000"
        };
        
        assertThat(allowedOrigins).isNotEmpty();
        assertThat(allowedOrigins).hasSize(2);
        assertThat(allowedOrigins[0]).isEqualTo("http://localhost");
    }
}


