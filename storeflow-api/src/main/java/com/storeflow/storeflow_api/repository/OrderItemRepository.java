package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for OrderItem entity providing data access operations.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all items for an order.
     */
    List<OrderItem> findByOrder_IdOrderByCreatedAtAsc(Long orderId);

    /**
     * Find all items for a product.
     */
    List<OrderItem> findByProduct_IdOrderByCreatedAtDesc(Long productId);

    /**
     * Count items in an order.
     */
    Long countByOrder_Id(Long orderId);

    /**
     * Get total quantity for a product across all orders.
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalQuantityOrdered(@Param("productId") Long productId);

    /**
     * Get order items with discount.
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.discountAmount > 0 ORDER BY oi.discountAmount DESC")
    List<OrderItem> findItemsWithDiscount();

    /**
     * Get most ordered products.
     */
    @Query(value = "SELECT oi.product_id, SUM(oi.quantity) as total_qty " +
                   "FROM order_items oi " +
                   "GROUP BY oi.product_id " +
                   "ORDER BY total_qty DESC", nativeQuery = true)
    List<Object[]> findMostOrderedProducts();
}
