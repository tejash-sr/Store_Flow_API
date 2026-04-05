package com.storeflow.storeflow_api.controller;

import com.storeflow.storeflow_api.dto.AuditLogDTO;
import com.storeflow.storeflow_api.entity.AuditLog;
import com.storeflow.storeflow_api.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Audit Log management.
 * Provides endpoints for querying and analyzing system-wide audit logs
 * for compliance, debugging, and activity tracking.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "APIs for viewing and analyzing system audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Get audit logs for a specific entity.
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Get audit logs for a specific entity", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsForEntity(
        @PathVariable String entityType,
        @PathVariable Long entityId) {
        log.info("Fetching audit logs for {} with ID: {}", entityType, entityId);
        List<AuditLog> auditLogs = auditLogService.getAuditLogsForEntity(entityType, entityId);
        List<AuditLogDTO> auditLogDTOs = auditLogs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(auditLogDTOs);
    }

    /**
     * Get paginated audit logs for a specific entity.
     */
    @GetMapping("/entity/{entityType}/{entityId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Get paginated audit logs for a specific entity", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogsForEntityPaginated(
        @PathVariable String entityType,
        @PathVariable Long entityId,
        Pageable pageable) {
        log.info("Fetching paginated audit logs for {} with ID: {}", entityType, entityId);
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsForEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(auditLogs.map(this::convertToDTO));
    }

    /**
     * Get audit logs for a specific user.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT') or @userService.isCurrentUser(#userId)")
    @Operation(summary = "Get audit logs for a user", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsForUser(@PathVariable Long userId) {
        log.info("Fetching audit logs for user ID: {}", userId);
        List<AuditLog> auditLogs = auditLogService.getAuditLogsForUser(userId);
        List<AuditLogDTO> auditLogDTOs = auditLogs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(auditLogDTOs);
    }

    /**
     * Get paginated audit logs for a specific user.
     */
    @GetMapping("/user/{userId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT') or @userService.isCurrentUser(#userId)")
    @Operation(summary = "Get paginated audit logs for a user", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogsForUserPaginated(
        @PathVariable Long userId,
        Pageable pageable) {
        log.info("Fetching paginated audit logs for user ID: {}", userId);
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsForUser(userId, pageable);
        return ResponseEntity.ok(auditLogs.map(this::convertToDTO));
    }

    /**
     * Get audit logs for a specific admin user.
     */
    @GetMapping("/admin/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs for an admin user", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsForAdmin(@PathVariable Long adminId) {
        log.info("Fetching audit logs for admin ID: {}", adminId);
        List<AuditLog> auditLogs = auditLogService.getAuditLogsForAdmin(adminId);
        List<AuditLogDTO> auditLogDTOs = auditLogs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(auditLogDTOs);
    }

    /**
     * Get audit logs for a specific action.
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs for a specific action", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsByAction(@PathVariable String action) {
        log.info("Fetching audit logs for action: {}", action);
        List<AuditLog> auditLogs = auditLogService.getAuditLogsByAction(action);
        List<AuditLogDTO> auditLogDTOs = auditLogs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(auditLogDTOs);
    }

    /**
     * Get audit logs within a date range.
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs within a date range", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsInDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching audit logs in date range: {} to {}", startDate, endDate);
        List<AuditLog> auditLogs = auditLogService.getAuditLogsInDateRange(startDate, endDate);
        List<AuditLogDTO> auditLogDTOs = auditLogs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(auditLogDTOs);
    }

    /**
     * Get paginated audit logs within a date range.
     */
    @GetMapping("/date-range/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get paginated audit logs within a date range", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogsInDateRangePaginated(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        Pageable pageable) {
        log.info("Fetching paginated audit logs in date range: {} to {}", startDate, endDate);
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsInDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(auditLogs.map(this::convertToDTO));
    }

    /**
     * Get all audit logs (paginated).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all audit logs (paginated)", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<Page<AuditLogDTO>> getAllAuditLogs(Pageable pageable) {
        log.info("Fetching all audit logs");
        Page<AuditLog> auditLogs = auditLogService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(auditLogs.map(this::convertToDTO));
    }

    /**
     * Convert AuditLog to AuditLogDTO.
     */
    private AuditLogDTO convertToDTO(AuditLog auditLog) {
        return AuditLogDTO.builder()
            .id(auditLog.getId())
            .entityType(auditLog.getEntityType())
            .entityId(auditLog.getEntityId())
            .action(auditLog.getAction())
            .userId(auditLog.getUser() != null ? auditLog.getUser().getId().toString() : null)
            .userName(auditLog.getUser() != null ? auditLog.getUser().getFullName() : null)
            .adminId(auditLog.getAdmin() != null ? auditLog.getAdmin().getId().toString() : null)
            .adminName(auditLog.getAdmin() != null ? auditLog.getAdmin().getFullName() : null)
            .oldValue(auditLog.getOldValue())
            .newValue(auditLog.getNewValue())
            .timestamp(auditLog.getTimestamp())
            .details(auditLog.getDetails())
            .status(auditLog.getStatus())
            .build();
    }
}
