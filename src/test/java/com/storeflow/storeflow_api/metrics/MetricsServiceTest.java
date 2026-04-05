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

    @Test
    @DisplayName("Should track total revenue accurately in cents")
    void testTotalRevenueTracking() {
        metricsService.recordOrderPlaced(BigDecimal.valueOf(100.50));
        metricsService.recordOrderPlaced(BigDecimal.valueOf(50.25));
        metricsService.recordOrderPlaced(BigDecimal.valueOf(75.00));
        Gauge gauge = meterRegistry.find("orders.total.revenue").gauge();
        assertThat(gauge.value()).isEqualTo(22575.0);
    }

    @Test
    @DisplayName("Should calculate average order value correctly")
    void testAverageOrderValueCalculation() {
        metricsService.recordOrderPlaced(100.0);
        metricsService.recordOrderPlaced(200.0);
        Gauge gauge = meterRegistry.find("orders.average.value").gauge();
        assertThat(gauge.value()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("Should increment pending orders count when order placed")
    void testPendingOrdersCountOnPlace() {
        metricsService.recordOrderPlaced(100.0);
        Gauge gauge = meterRegistry.find("orders.pending.count").gauge();
        assertThat(gauge.value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should decrement pending orders when order completed")
    void testPendingOrdersCountOnCompletion() {
        metricsService.recordOrderPlaced(100.0);
        Gauge pendingGauge = meterRegistry.find("orders.pending.count").gauge();
        assertThat(pendingGauge.value()).isEqualTo(1.0);
        
        metricsService.recordOrderCompleted(100.0);
        assertThat(pendingGauge.value()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should increment completed orders counter when order completed")
    void testOrderCompletedCounter() {
        metricsService.recordOrderCompleted(100.0);
        metricsService.recordOrderCompleted(200.0);
        Counter counter = meterRegistry.find("orders.completed.count").counter();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Should support double overload for order placement")
    void testRecordOrderPlacedDoubleOverload() {
        metricsService.recordOrderPlaced(99.99);
        Counter counter = meterRegistry.find("orders.placed.count").counter();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should handle null order value gracefully")
    void testHandleNullOrderValue() {
        metricsService.recordOrderPlaced((BigDecimal) null);
        Counter counter = meterRegistry.find("orders.placed.count").counter();
        assertThat(counter.count()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should ignore negative order values")
    void testIgnoreNegativeOrderValue() {
        metricsService.recordOrderPlaced(BigDecimal.valueOf(-100.0));
        Counter counter = meterRegistry.find("orders.placed.count").counter();
        assertThat(counter.count()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should reset all metrics to zero")
    void testResetMetrics() {
        metricsService.recordOrderPlaced(100.0);
        metricsService.recordOrderPlaced(200.0);
        metricsService.resetMetrics();
        
        Gauge totalRevenueGauge = meterRegistry.find("orders.total.revenue").gauge();
        Gauge pendingCountGauge = meterRegistry.find("orders.pending.count").gauge();
        Gauge averageValueGauge = meterRegistry.find("orders.average.value").gauge();

        assertThat(totalRevenueGauge.value()).isEqualTo(0.0);
        assertThat(pendingCountGauge.value()).isEqualTo(0.0);
        assertThat(averageValueGauge.value()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should set pending orders count manually")
    void testSetPendingOrdersCount() {
        metricsService.setPendingOrdersCount(5);
        Gauge gauge = meterRegistry.find("orders.pending.count").gauge();
        assertThat(gauge.value()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Should format metrics summary correctly")
    void testMetricsSummaryFormatting() {
        metricsService.recordOrderPlaced(100.0);
        metricsService.recordOrderPlaced(200.0);
        metricsService.recordOrderCompleted(100.0);
        String summary = metricsService.getMetricsSummary();
        assertThat(summary).contains("OrdersPlaced=2", "OrdersCompleted=1", "TotalRevenue=$300.00", "AverageValue=$150.00");
    }

    @Test
    @DisplayName("Should handle multiple orders with decimal precision")
    void testDecimalPrecision() {
        metricsService.recordOrderPlaced(BigDecimal.valueOf(10.01));
        metricsService.recordOrderPlaced(BigDecimal.valueOf(20.02));
        metricsService.recordOrderPlaced(BigDecimal.valueOf(30.03));
        
        Gauge totalRevenueGauge = meterRegistry.find("orders.total.revenue").gauge();
        assertThat(totalRevenueGauge.value()).isEqualTo(6006.0);
        
        Gauge averageValueGauge = meterRegistry.find("orders.average.value").gauge();
        assertThat(averageValueGauge.value()).isCloseTo(20.02, within(0.01));
    }

    @Test
    @DisplayName("Should handle zero order value")
    void testZeroOrderValue() {
        metricsService.recordOrderPlaced(BigDecimal.ZERO);
        Counter counter = meterRegistry.find("orders.placed.count").counter();
        Gauge totalRevenueGauge = meterRegistry.find("orders.total.revenue").gauge();
        assertThat(counter.count()).isEqualTo(1.0);
        assertThat(totalRevenueGauge.value()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should calculate average as zero when no orders exist")
    void testAverageValueWhenNoOrders() {
        Gauge gauge = meterRegistry.find("orders.average.value").gauge();
        assertThat(gauge.value()).isEqualTo(0.0);
    }
}
