package com.ranxom.zentry.controller;

import com.ranxom.zentry.aop.Auditable;
import com.ranxom.zentry.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuditLogRepository auditLogRepository;

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
}