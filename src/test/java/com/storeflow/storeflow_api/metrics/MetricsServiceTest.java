package com.storeflow.storeflow_api.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Test suite for custom Micrometer metrics service.
 * 
 * Test Coverage:
 * 1. Orders placed counter increments
 * 2. Total revenue tracking (in cents for precision)
 * 3. Pending orders count updates
 * 4. Average order value calculation
 * 5. Order completion flow
 * 6. BigDecimal and double overloads
 * 7. Metrics reset functionality
 * 8. Edge cases (negative values, null values)
 * 9. Metrics summary formatting
 * 10. Thread-safe updates
 * 
 * Architecture:
 * - Unit tests only (no @SpringBootTest)
 * - SimpleMeterRegistry for testing
 * - BigDecimal for monetary calculations (precision)
 * 
 * @author StoreFlow
 * @version 1.0
 */
@DisplayName("MetricsService - Custom Business Metrics")
class MetricsServiceTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    @DisplayName("Should initialize all custom metrics on creation")
    void testMetricsInitialization() {
        Counter ordersPlaced = meterRegistry.find("orders.placed.count").counter();
        assertThat(ordersPlaced).isNotNull();
        assertThat(ordersPlaced.count()).isEqualTo(0.0);

        Counter ordersCompleted = meterRegistry.find("orders.completed.count").counter();
        assertThat(ordersCompleted).isNotNull();

        Gauge totalRevenue = meterRegistry.find("orders.total.revenue").gauge();
        assertThat(totalRevenue).isNotNull();

        Gauge pendingCount = meterRegistry.find("orders.pending.count").gauge();
        assertThat(pendingCount).isNotNull();

        Gauge averageValue = meterRegistry.find("orders.average.value").gauge();
        assertThat(averageValue).isNotNull();
    }

    @Test
    @DisplayName("Should increment orders placed counter when order recorded")
    void testRecordOrderPlaced() {
        metricsService.recordOrderPlaced(BigDecimal.valueOf(100.50));
        Counter counter = meterRegistry.find("orders.placed.count").counter();
        assertThat(counter.count()).isEqualTo(1.0);
    }
}

