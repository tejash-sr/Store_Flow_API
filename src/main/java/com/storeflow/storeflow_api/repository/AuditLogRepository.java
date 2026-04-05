package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity.
 * Provides database access methods for managing audit logs, including
 * querying by entity type, user, action, timestamp, and status.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for an entity.
     */
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find paginated audit logs for an entity.
     */
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    /**
     * Find all audit logs for a user.
     */
    List<AuditLog> findByUserId(Long userId);

    /**
     * Find paginated audit logs for a user.
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all audit logs for an admin user.
     */
    List<AuditLog> findByAdminId(Long adminId);

    /**
     * Find paginated audit logs for an admin user.
     */
    Page<AuditLog> findByAdminId(Long adminId, Pageable pageable);

    /**
     * Find all audit logs for a specific action.
     */
    List<AuditLog> findByAction(String action);

    /**
     * Find paginated audit logs for a specific action.
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Find audit logs for a specific entity type.
     */
    List<AuditLog> findByEntityType(String entityType);

    /**
     * Find paginated audit logs for a specific entity type.
     */
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * Find audit logs from successful operations only.
     */
    List<AuditLog> findByStatus(String status);

    /**
     * Find paginated audit logs from successful operations only.
     */
    Page<AuditLog> findByStatus(String status, Pageable pageable);

    /**
     * Find audit logs within a date range.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :startDate AND a.timestamp <= :endDate " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findAuditLogsInDateRange(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * Find paginated audit logs within a date range.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :startDate AND a.timestamp <= :endDate " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findAuditLogsInDateRange(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

    /**
     * Find audit logs for an entity within a date range.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType " +
           "AND a.entityId = :entityId " +
           "AND a.timestamp >= :startDate AND a.timestamp <= :endDate " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findAuditLogsForEntityInDateRange(@Param("entityType") String entityType,
                                                     @Param("entityId") Long entityId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Count audit logs by action type.
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action")
    Long countByAction(@Param("action") String action);

    /**
     * Count audit logs by user (regular user operations).
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId")
    Long countByUser(@Param("userId") Long userId);

    /**
     * Find all audit logs ordered by timestamp descending.
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    Page<AuditLog> findAllOrderByTimestampDesc(Pageable pageable);
}
