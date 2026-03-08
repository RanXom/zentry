package com.ranxom.zentry.controller;

import com.ranxom.zentry.dto.AdminUserUpdate;
import com.ranxom.zentry.dto.UserResponse;
import com.ranxom.zentry.repository.AuditLogRepository;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.security.CustomUserDetailsService;
import com.ranxom.zentry.services.AdminService;
import com.ranxom.zentry.services.RateLimiterService;
import com.ranxom.zentry.services.TokenBlacklistService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.AuthenticationProvider;

import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.context.annotation.Import;
import com.ranxom.zentry.config.SecurityConfig;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    public AdminControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private AdminService adminService;
    @MockitoBean private AuditLogRepository auditLogRepository;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private TokenBlacklistService blacklistService;
    @MockitoBean private RateLimiterService rateLimiterService;
    @MockitoBean private AuthenticationProvider authenticationProvider;

    @Test
    @WithMockUser(authorities = "SYSTEM_READ")
    void getAllUsers_ShouldSucceed_WhenAdmin() throws Exception {
        UserResponse response = UserResponse.builder().username("ranxom").build();
        when(adminService.getAllUsers()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ranxom"));
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    void toggleUserLock_ShouldSucceed_WhenAuthorized() throws Exception {
        mockMvc.perform(patch("/api/admin/users/1/toggle-lock").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Identity state has been altered."));
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    void modifyUser_ShouldReshapeIdentity() throws Exception {
        AdminUserUpdate update = new AdminUserUpdate();
        update.setUsername("new_shizain");

        mockMvc.perform(put("/api/admin/users/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER") // No permissions, just the base role
    void logs_ShouldForbidden_WhenStandardUser() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isForbidden());
    }

}
