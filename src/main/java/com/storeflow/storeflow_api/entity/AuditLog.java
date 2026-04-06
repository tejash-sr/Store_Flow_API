package com.storeflow.storeflow_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AuditLog entity for comprehensive tracking of system actions and user operations.
 * Records create, update, delete, approval, and rejection operations with full
 * context including user info, old/new values, and detailed action descriptions
 * per system PDF specification.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_log_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_log_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_log_admin_id", columnList = "admin_id"),
    @Index(name = "idx_audit_log_action", columnList = "action"),
    @Index(name = "idx_audit_log_timestamp", columnList = "timestamp DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 50)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_audit_log_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", foreignKey = @ForeignKey(name = "fk_audit_log_admin"))
    private User admin;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 50)
    @Builder.Default
    private String status = "SUCCESS";

    /**
     * Create an audit log for a create action.
     */
    public static AuditLog createCreateLog(String entityType, Long entityId, String newValue, 
                                          User user, String details) {
        return AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action("CREATE")
            .user(user)
            .newValue(newValue)
            .status("SUCCESS")
            .details(details)
            .build();
    }

    /**
     * Create an audit log for an update action.
     */
    public static AuditLog createUpdateLog(String entityType, Long entityId, String oldValue, 
                                          String newValue, User user, String details) {
        return AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action("UPDATE")
            .user(user)
            .oldValue(oldValue)
            .newValue(newValue)
            .status("SUCCESS")
            .details(details)
            .build();
    }

    /**
     * Create an audit log for a delete action.
     */
    public static AuditLog createDeleteLog(String entityType, Long entityId, String oldValue, 
                                          User user, String details) {
        return AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action("DELETE")
            .user(user)
            .oldValue(oldValue)
            .status("SUCCESS")
            .details(details)
            .build();
    }

    /**
     * Create an audit log for an approval action (admin action).
     */
    public static AuditLog createApprovalLog(String entityType, Long entityId, User admin, 
                                            String details) {
        return AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action("APPROVE")
            .admin(admin)
            .status("SUCCESS")
            .details(details)
            .build();
    }

    /**
     * Create an audit log for a rejection action (admin action).
     */
    public static AuditLog createRejectionLog(String entityType, Long entityId, User admin, 
                                             String details) {
        return AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action("REJECT")
            .admin(admin)
            .status("SUCCESS")
            .details(details)
            .build();
    }

    /**
     * Mark the audit log entry as failed.
     */
    public void markAsFailure() {
        this.status = "FAILURE";
    }

    /**
     * Mark the audit log entry as pending.
     */
    public void markAsPending() {
        this.status = "PENDING";
    }

    /**
     * Check if the action was successful.
     */
    public boolean wasSuccessful() {
        return "SUCCESS".equals(status);
    }
}
