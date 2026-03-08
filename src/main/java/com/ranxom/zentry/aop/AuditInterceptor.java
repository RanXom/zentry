package com.ranxom.zentry.aop;

import com.ranxom.zentry.model.AuditLog;
import com.ranxom.zentry.repository.AuditLogRepository;
import com.ranxom.zentry.security.ZentryUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditInterceptor {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAction(JoinPoint joinPoint, Auditable auditable, Object result) {
        // Identify the 'Actor' from the Security Context
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long actorId = (auth != null && auth.getPrincipal() instanceof ZentryUserDetails details)
                ? details.user().getId() : null;

        Map<String, Object> detailsMap = new HashMap<>();
        Object[] args = joinPoint.getArgs();

        if (args.length > 0 && args[0] != null) {
            detailsMap.put("input_summary", "Payload processed for action: " + auditable.action());
        }

        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .actionType(auditable.action())
                .ipAddress(request.getRemoteAddr())
                .details(detailsMap)
                .build();

        // Commit to the immutable ledger
        auditLogRepository.save(log);
    }
}