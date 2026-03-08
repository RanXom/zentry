package com.ranxom.zentry.controller;

import com.ranxom.zentry.aop.Auditable;
import com.ranxom.zentry.dto.AdminUserUpdate;
import com.ranxom.zentry.dto.UserResponse;
import com.ranxom.zentry.repository.AuditLogRepository;
import com.ranxom.zentry.services.AdminService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuditLogRepository auditLogRepository;
    private final AdminService adminService;

    public AdminController(
            AuditLogRepository auditLogRepository,
            AdminService adminService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.adminService = adminService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('SYSTEM_READ')") // Only Admins with this power can pass hehe
    @Auditable(action = "ADMIN_VIEWED_AUDIT_LOGS")
    public ResponseEntity<?> getAllLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Check for the role itself
    public ResponseEntity<String> getSystemStatus() {
        return ResponseEntity.ok("Sentinel Status: Optimal. Redis and Postgres are in sync.");
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SYSTEM_READ')")
    @Auditable(action = "ADMIN_VIEWED_USER_LIST")
    @Transactional
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/toggle-lock")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    @Auditable(action = "ADMIN_TOGGLED_USER_LOCK")
    public ResponseEntity<String> toggleUserLock(@PathVariable Long id) {
        adminService.toggleLock(id);
        return ResponseEntity.ok("Identity state has been altered.");
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')") // Only high-level admins
    @Auditable(action = "ADMIN_MODIFIED_USER_DATA")
    public ResponseEntity<String> modifyUser(@PathVariable Long id, @RequestBody AdminUserUpdate update) {
        adminService.updateUser(id, update);
        return ResponseEntity.ok("Identity " + id + " has been reshaped by administrative decree.");
    }

}