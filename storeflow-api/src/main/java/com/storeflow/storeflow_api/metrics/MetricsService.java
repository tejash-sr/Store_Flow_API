package com.storeflow.storeflow_api.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Custom Micrometer metrics service for StoreFlow business metrics.
 * 
 * Metrics tracked:
 * 1. orders.placed.count - Total number of orders placed (counter)
 * 2. orders.total.revenue - Cumulative revenue from all orders (gauge)
 * 3. orders.average.value - Average order value (gauge)
 * 4. orders.pending.count - Number of pending orders (gauge)
 * 5. orders.completed.count - Number of completed orders (counter)
 * 
 * Usage:
 * ```java
 * metricsService.recordOrderPlaced(100.50);
 * metricsService.recordOrderCompleted();
 * metricsService.updateAverageOrderValue(75.25);
 * ```
 * 
 * Export:
 * - Prometheus: /actuator/prometheus
 * - Health: /actuator/health
 * - Metrics: /actuator/metrics
 * 
 * @author StoreFlow
 * @version 1.0
 */
@Slf4j
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Counters
    private Counter ordersPlacedCounter;
    private Counter ordersCompletedCounter;

    // Gauges (Atomic references for thread-safe updates)
    private AtomicLong totalRevenue;
    private AtomicLong pendingOrdersCount;
    private AtomicReference<Double> averageOrderValue;

    /**
     * Initialize all custom metrics in the Micrometer registry.
     * Called automatically on bean creation.
     */
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    /**
     * Initialize all custom metrics with appropriate tags and descriptions.
     */
    private void initializeMetrics() {
        // Counter: Orders placed
        this.ordersPlacedCounter = Counter.builder("orders.placed.count")
                .description("Total number of orders placed")
                .baseUnit("orders")
                .tag("environment", getEnvironment())
                .register(meterRegistry);
        log.info("Initialized metric: orders.placed.count");

        // Counter: Orders completed
        this.ordersCompletedCounter = Counter.builder("orders.completed.count")
                .description("Total number of completed orders")
                .baseUnit("orders")
                .tag("environment", getEnvironment())
                .register(meterRegistry);
        log.info("Initialized metric: orders.completed.count");

        // Gauge: Total revenue
        this.totalRevenue = new AtomicLong(0);
        meterRegistry.gauge("orders.total.revenue",
                Tags.of("currency", "USD", "environment", getEnvironment()),
                totalRevenue,
                AtomicLong::get);
        log.info("Initialized metric: orders.total.revenue");

        // Gauge: Pending orders count
        this.pendingOrdersCount = new AtomicLong(0);
        meterRegistry.gauge("orders.pending.count",
                Tags.of("environment", getEnvironment()),
                pendingOrdersCount,
                AtomicLong::get);
        log.info("Initialized metric: orders.pending.count");

        // Gauge: Average order value
        this.averageOrderValue = new AtomicReference<>(0.0);
        meterRegistry.gauge("orders.average.value",
                Tags.of("currency", "USD", "environment", getEnvironment()),
                averageOrderValue,
                AtomicReference::get);
        log.info("Initialized metric: orders.average.value");
    }

    /**
     * Record a new order placement with its value.
     * Increments orders.placed.count and updates total revenue.
     *
     * @param orderValue The monetary value of the order
     */
    public void recordOrderPlaced(BigDecimal orderValue) {
        if (orderValue == null || orderValue.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Invalid order value: {}. Skipping metrics update.", orderValue);
            return;
        }

        // Increment orders placed counter
        ordersPlacedCounter.increment();

        // Add to total revenue (convert to cents for precision)
        long orderValueCents = orderValue.movePointRight(2).longValue();
        totalRevenue.addAndGet(orderValueCents);

        // Increment pending orders
        pendingOrdersCount.incrementAndGet();

        // Recalculate average after adding order
        updateAverageOrderValue(orderValue);

        log.debug("Order recorded: value={}, total_revenue={}, pending_orders={}",
                orderValue, totalRevenue.get() / 100.0, pendingOrdersCount.get());
    }

    /**
     * Record a simple order placement (overload for convenience).
     *
     * @param orderValue The monetary value of the order as double
     */
    public void recordOrderPlaced(double orderValue) {
        recordOrderPlaced(BigDecimal.valueOf(orderValue));
    }

    /**
     * Record order completion and update metrics.
     * Decrements pending orders and increments completed orders counter.
     *
     * @param orderValue The order value for average calculation
     */
    public void recordOrderCompleted(BigDecimal orderValue) {
        ordersCompletedCounter.increment();
        
        long pendingCount = pendingOrdersCount.get();
        if (pendingCount > 0) {
            pendingOrdersCount.decrementAndGet();
        }

        // Update average order value
        updateAverageOrderValue(orderValue);

        log.debug("Order completed: value={}, pending_orders={}",
                orderValue, pendingOrdersCount.get());
    }

    /**
     * Record order completion (overload for double).
     *
     * @param orderValue The order value as double
     */
    public void recordOrderCompleted(double orderValue) {
        recordOrderCompleted(BigDecimal.valueOf(orderValue));
    }

    /**
     * Update the average order value metric.
     * Average = Total Revenue / Orders Placed
     *
     * @param orderValue The latest order value for calculation
     */
    public void updateAverageOrderValue(BigDecimal orderValue) {
        long placedCount = (long) ordersPlacedCounter.count();
        if (placedCount <= 0) {
            averageOrderValue.set(0.0);
            return;
        }

        double averageValue = (totalRevenue.get() / 100.0) / placedCount;
        averageOrderValue.set(averageValue);

        log.debug("Average order value updated: {}", averageValue);
    }

    /**
     * Update pending orders count manually (for initialization or sync).
     *
     * @param count The number of pending orders
     */
    public void setPendingOrdersCount(long count) {
        pendingOrdersCount.set(count);
        log.info("Pending orders count set to: {}", count);
    }

    /**
     * Reset all metrics (useful for testing).
     */
    public void resetMetrics() {
        totalRevenue.set(0);
        pendingOrdersCount.set(0);
        averageOrderValue.set(0.0);
        log.info("All custom metrics reset");
    }

    /**
     * Get current metric values snapshot.
     *
     * @return String representation of current metrics
     */
    public String getMetricsSummary() {
        return String.format(
                "Metrics Summary: OrdersPlaced=%d, OrdersCompleted=%d, TotalRevenue=$%.2f, PendingOrders=%d, AverageValue=$%.2f",
                (long) ordersPlacedCounter.count(),
                (long) ordersCompletedCounter.count(),
                totalRevenue.get() / 100.0,
                pendingOrdersCount.get(),
                averageOrderValue.get()
        );
    }

    /**
     * Get environment name for metric tags.
     *
     * @return Environment name (default: "dev")
     */
    private String getEnvironment() {
        return System.getProperty("spring.profiles.active", "dev");
    }
}
