package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity providing data access operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number.
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find all orders for a store.
     */
    List<Order> findByStore_IdOrderByCreatedAtDesc(Long storeId);

    /**
     * Find orders by status.
     */
    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);

    /**
     * Find pending orders.
     */
    List<Order> findByStatusOrderByCreatedAtAsc(Order.OrderStatus status);

    /**
     * Find orders created within date range.
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find orders for customer by email.
     */
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    /**
     * Find orders with items using JOIN FETCH.
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    /**
     * Count orders by status.
     */
    Long countByStatus(Order.OrderStatus status);

    /**
     * Count orders for a store.
     */
    Long countByStore_Id(Long storeId);

    /**
     * Find recent orders (limit 10).
     */
    @Query(value = "SELECT o FROM Order o ORDER BY o.createdAt DESC", nativeQuery = false)
    List<Order> findRecentOrders();
}
