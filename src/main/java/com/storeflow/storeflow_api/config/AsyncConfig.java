package com.storeflow.storeflow_api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration
 * - Configures ThreadPoolTaskExecutor for @Async methods
 * - Handles background job processing with configurable thread pool
 * - Provides graceful error handling for async tasks
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Email executor - for handling email sending tasks
     * Core: 2 threads, Max: 5 threads, Queue: 100 tasks
     */
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("storeflow-email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("Email executor initialized: coreSize=2, maxSize=5, queue=100");
        return executor;
    }
    
    /**
     * PDF report executor - for handling PDF generation tasks
     * Core: 3 threads, Max: 8 threads, Queue: 200 tasks
     */
    @Bean(name = "pdfExecutor")
    public Executor pdfExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("storeflow-pdf-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        log.info("PDF executor initialized: coreSize=3, maxSize=8, queue=200");
        return executor;
    }
    
    /**
     * General async executor - for miscellaneous async tasks
     * Core: 2 threads, Max: 4 threads, Queue: 50 tasks
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("storeflow-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("Async executor initialized: coreSize=2, maxSize=4, queue=50");
        return executor;
    }
}
