package com.ranxom.zentry.aop;

import com.ranxom.zentry.model.AuditLog;
import com.ranxom.zentry.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditInterceptorTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock
    private HttpServletRequest request;
    @InjectMocks
    private AuditInterceptor auditInterceptor;

    @Test
    void interceptorShouldApplyMaskingBeforeSaving() throws Throwable {
        // Arrange
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Auditable auditable = mock(Auditable.class);
        when(auditable.action()).thenReturn("IDENTITY_AUTHENTICATED");
        when(joinPoint.proceed()).thenReturn(new Object());
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Act
        auditInterceptor.logAction(joinPoint, auditable);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();

        assertNotNull(savedLog.getDetails());

        assertEquals("127.0.0.1", savedLog.getIpAddress());
        assertEquals("IDENTITY_AUTHENTICATED", savedLog.getActionType());
    }

}
