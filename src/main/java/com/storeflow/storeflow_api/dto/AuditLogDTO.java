package com.storeflow.storeflow_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for AuditLog.
 * Used for API responses for audit log data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;

    private String entityType;

    private Long entityId;

    private String action;

    private String userId;

    private String userName;

    private String adminId;

    private String adminName;

    private String oldValue;

    private String newValue;

    private LocalDateTime timestamp;

    private String details;

    private String status;
}
