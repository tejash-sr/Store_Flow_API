package com.storeflow.storeflow_api.config;

import com.storeflow.storeflow_api.security.WebSocketAuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * WebSocket configuration for real-time inventory alerts.
 * 
 * Enables STOMP messaging protocol over WebSocket connections for live notifications
 * of inventory changes, low-stock alerts, and order updates.
 * 
 * Configuration includes:
 * - STOMP endpoint registration at /ws/alerts
 * - SockJS fallback for browsers without WebSocket support
 * - Message broker configuration with /topic for broadcast messages
 * - User-specific destinations with /queue for private messages
 * 
 * @author StoreFlow
 * @version 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired(required = false)
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * Configure STOMP endpoints for WebSocket connections.
     * 
     * Registers:
     * - /ws/alerts as the STOMP endpoint for client connections
     * - SockJS fallback for browsers without native WebSocket support
     * - Allows cross-origin connections from localhost (development)
     * 
     * @param registry the STOMP endpoint registry for configuring connection endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        var endpointConfig = registry
            .addEndpoint("/ws/alerts")
            .setAllowedOrigins("http://localhost", "http://localhost:*", "http://localhost:3000")
            .setHandshakeHandler(new DefaultHandshakeHandler());
        
        // Register JWT auth interceptor if available (may be null in some test contexts)
        if (webSocketAuthInterceptor != null) {
            endpointConfig.addInterceptors(webSocketAuthInterceptor);
        }
        
        endpointConfig.withSockJS()
                .setStreamBytesLimit(512 * 1024)
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000);
    }

    /**
     * Configure the message broker for routing messages.
     * 
     * Sets up:
     * - /topic prefix for broadcast messages (one-to-many)
     * - /queue prefix for user-specific messages (one-to-one)
     * - /app prefix for application-level message handling
     * 
     * Uses a simple in-memory broker suitable for single-server deployments.
     * For production with multiple instances, consider adding RabbitMQ or Kafka.
     * 
     * @param config the message broker registry for configuring message routing
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("websocket-broker-");
        taskScheduler.initialize();
        
        config
            .enableSimpleBroker("/topic", "/queue")
            .setTaskScheduler(taskScheduler)
            .setHeartbeatValue(new long[]{25000, 25000});
        
        config.setApplicationDestinationPrefixes("/app");
    }
}
