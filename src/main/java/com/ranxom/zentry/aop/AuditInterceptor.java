package com.ranxom.zentry.aop;

import com.ranxom.zentry.dto.RegisterRequest;
import com.ranxom.zentry.model.AuditLog;
import com.ranxom.zentry.repository.AuditLogRepository;
import com.ranxom.zentry.security.ZentryUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditInterceptor {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    @Around("@annotation(auditable)")
    public Object logAction(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // Identify the 'Actor' from the Security Context
        Object result = joinPoint.proceed();

        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            Long actorId = (auth != null && auth.getPrincipal() instanceof ZentryUserDetails details)
                    ? details.user().getId() : null;

            Map<String, Object> detailsMap = new HashMap<>();
            Long targetEntityId = null;
            Object[] args = joinPoint.getArgs();

            switch (auditable.action()) {
                case "IDENTITY_REGISTERED" -> {
                    if (args.length > 0 && args[0] instanceof RegisterRequest reg) {
                        detailsMap.put("registered_username", reg.getUsername());
                        detailsMap.put("registered_email", reg.getEmail());
                    }
                }
                case "IDENTITY_AUTHENTICATED" -> {
                    detailsMap.put("status", "Login successful");
                }
                case "ADMIN_VIEWED_AUDIT_LOGS" -> {
                    detailsMap.put("scope", "system_wide_audit");
                }
                case "IDENTITY_EXILED" -> {
                    detailsMap.put("reason", "User requested logout");
                }
            }

            AuditLog auditEntry = AuditLog.builder()
                    .actorId(actorId)
                    .actionType(auditable.action())
                    .targetEntityId(targetEntityId)
                    .ipAddress(request.getRemoteAddr())
                    .details(detailsMap)
                    .build();

            auditLogRepository.save(auditEntry);

        } catch (Exception e) {
            log.error("Watcher Error: Failed to record action for {}", auditable.action(), e);
        }

        return result;
    }
}