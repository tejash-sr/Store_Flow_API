package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.entity.AuditLog;
import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing audit logs.
 * Handles business logic for creating, querying, and analyzing audit logs
 * for comprehensive system activity tracking and compliance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log a create action.
     */
    public AuditLog logCreate(String entityType, Long entityId, String newValue, User user, String details) {
        log.debug("Logging CREATE action for {} with ID: {}", entityType, entityId);
        AuditLog auditLog = AuditLog.createCreateLog(entityType, entityId, newValue, user, details);
        return auditLogRepository.save(auditLog);
    }

    /**
     * Log an update action.
     */
    public AuditLog logUpdate(String entityType, Long entityId, String oldValue, String newValue, User user, String details) {
        log.debug("Logging UPDATE action for {} with ID: {}", entityType, entityId);
        AuditLog auditLog = AuditLog.createUpdateLog(entityType, entityId, oldValue, newValue, user, details);
        return auditLogRepository.save(auditLog);
    }

    /**
     * Log a delete action.
     */
    public AuditLog logDelete(String entityType, Long entityId, String oldValue, User user, String details) {
        log.debug("Logging DELETE action for {} with ID: {}", entityType, entityId);
        AuditLog auditLog = AuditLog.createDeleteLog(entityType, entityId, oldValue, user, details);
        return auditLogRepository.save(auditLog);
    }

    /**
     * Log an approval action (admin action).
     */
    public AuditLog logApproval(String entityType, Long entityId, User admin, String details) {
        log.debug("Logging APPROVAL action for {} with ID: {} by admin", entityType, entityId);
        AuditLog auditLog = AuditLog.createApprovalLog(entityType, entityId, admin, details);
        return auditLogRepository.save(auditLog);
    }

    /**
     * Log a rejection action (admin action).
     */
    public AuditLog logRejection(String entityType, Long entityId, User admin, String details) {
        log.debug("Logging REJECTION action for {} with ID: {} by admin", entityType, entityId);
        AuditLog auditLog = AuditLog.createRejectionLog(entityType, entityId, admin, details);
        return auditLogRepository.save(auditLog);
    }

    /**
     * Get all audit logs for an entity.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForEntity(String entityType, Long entityId) {
        log.debug("Fetching audit logs for {} with ID: {}", entityType, entityId);
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Get paginated audit logs for an entity.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForEntity(String entityType, Long entityId, Pageable pageable) {
        log.debug("Fetching paginated audit logs for {} with ID: {}", entityType, entityId);
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    /**
     * Get audit logs for a user.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForUser(Long userId) {
        log.debug("Fetching audit logs for user ID: {}", userId);
        return auditLogRepository.findByUserId(userId);
    }

    /**
     * Get paginated audit logs for a user.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForUser(Long userId, Pageable pageable) {
        log.debug("Fetching paginated audit logs for user ID: {}", userId);
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * Get audit logs for an admin user.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForAdmin(Long adminId) {
        log.debug("Fetching audit logs for admin ID: {}", adminId);
        return auditLogRepository.findByAdminId(adminId);
    }

    /**
     * Get paginated audit logs for an admin user.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForAdmin(Long adminId, Pageable pageable) {
        log.debug("Fetching paginated audit logs for admin ID: {}", adminId);
        return auditLogRepository.findByAdminId(adminId, pageable);
    }

    /**
     * Get audit logs for a specific action.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByAction(String action) {
        log.debug("Fetching audit logs for action: {}", action);
        return auditLogRepository.findByAction(action);
    }

    /**
     * Get paginated audit logs for a specific action.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        log.debug("Fetching paginated audit logs for action: {}", action);
        return auditLogRepository.findByAction(action, pageable);
    }

    /**
     * Get audit logs within a date range.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching audit logs in date range: {} to {}", startDate, endDate);
        return auditLogRepository.findAuditLogsInDateRange(startDate, endDate);
    }

    /**
     * Get paginated audit logs within a date range.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsInDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Fetching paginated audit logs in date range: {} to {}", startDate, endDate);
        return auditLogRepository.findAuditLogsInDateRange(startDate, endDate, pageable);
    }

    /**
     * Get audit logs for an entity within a date range.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForEntityInDateRange(String entityType, Long entityId, 
                                                           LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching audit logs for {} with ID: {} in date range: {} to {}", 
                  entityType, entityId, startDate, endDate);
        return auditLogRepository.findAuditLogsForEntityInDateRange(entityType, entityId, startDate, endDate);
    }

    /**
     * Get all audit logs (paginated).
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        log.debug("Fetching all audit logs");
        return auditLogRepository.findAllOrderByTimestampDesc(pageable);
    }

    /**
     * Count audit logs by action.
     */
    @Transactional(readOnly = true)
    public Long countByAction(String action) {
        log.debug("Counting audit logs for action: {}", action);
        return auditLogRepository.countByAction(action);
    }

    /**
     * Count audit logs by user.
     */
    @Transactional(readOnly = true)
    public Long countByUser(Long userId) {
        log.debug("Counting audit logs for user ID: {}", userId);
        return auditLogRepository.countByUser(userId);
    }

    /**
     * Delete audit logs older than a specified date.
     */
    public void deleteOldAuditLogs(LocalDateTime cutoffDate) {
        log.info("Starting deletion of audit logs older than: {}", cutoffDate);
        List<AuditLog> oldLogs = auditLogRepository.findAuditLogsInDateRange(
            LocalDateTime.of(1970, 1, 1, 0, 0), cutoffDate);
        auditLogRepository.deleteAll(oldLogs);
        log.info("Deleted {} old audit logs", oldLogs.size());
    }
}
