package com.ranxom.zentry.controller;

import com.ranxom.zentry.config.SecurityConfig;
import com.ranxom.zentry.exception.GlobalExceptionHandler;
import com.ranxom.zentry.repository.AuditLogRepository;
import com.ranxom.zentry.security.CustomUserDetailsService;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.services.RateLimiterService;
import com.ranxom.zentry.services.TokenBlacklistService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public abstract class BaseControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @MockitoBean protected CustomUserDetailsService customUserDetailsService;
    @MockitoBean protected RateLimiterService rateLimiterService;
    @MockitoBean protected JwtService jwtService;
    @MockitoBean protected TokenBlacklistService blacklistService;
    @MockitoBean protected AuditLogRepository auditLogRepository;
    @MockitoBean protected AuthenticationProvider authenticationProvider;

    @BeforeEach
    void setUpRateLimiting() {
        Bucket mockBucket = mock(Bucket.class);
        // Tell the mock bucket to always allow the request
        when(mockBucket.tryConsume(anyLong())).thenReturn(true);
        // Tell the service to return this mock bucket for any IP
        when(rateLimiterService.resolveBucket(anyString())).thenReturn(mockBucket);
    }

}