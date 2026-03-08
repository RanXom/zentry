package com.ranxom.zentry.controller;

import com.ranxom.zentry.config.SecurityConfig;
import com.ranxom.zentry.exception.GlobalExceptionHandler;
import com.ranxom.zentry.repository.AuditLogRepository;
import com.ranxom.zentry.security.CustomUserDetailsService;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.services.RateLimiterService;
import com.ranxom.zentry.services.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Common Security Mocks to satisfy the Filter Chain
    @MockitoBean protected CustomUserDetailsService customUserDetailsService;
    @MockitoBean protected RateLimiterService rateLimiterService;
    @MockitoBean protected JwtService jwtService;
    @MockitoBean protected TokenBlacklistService blacklistService;
    @MockitoBean protected AuditLogRepository auditLogRepository;
    @MockitoBean protected AuthenticationProvider authenticationProvider;

}