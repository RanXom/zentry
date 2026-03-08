package com.ranxom.zentry.controller;

import com.ranxom.zentry.dto.*;
import com.ranxom.zentry.repository.RefreshTokenRepository;
import com.ranxom.zentry.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends BaseControllerTest {

    @MockitoBean private RegisterService registerService;
    @MockitoBean private AuthenticateService authenticateService;
    @MockitoBean private RefreshService refreshService;
    @MockitoBean private RefreshTokenRepository refreshTokenRepository;

    @Test
    void register_ShouldForgeNewIdentity() throws Exception {
        RegisterRequest request = new RegisterRequest("shizain", "shizain@zentry.io", "NoblePass123!");
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("mock-access")
                .refreshToken("mock-refresh")
                .build();

        when(registerService.execute(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register").with(csrf()) // Mutation requires CSRF
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access"));
    }

    @Test
    void login_ShouldReturnTokens_OnValidCredentials() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("shizain", "NoblePass123!");
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("mock-access")
                .build();

        when(authenticateService.execute(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access"));
    }

    @Test
    void refresh_ShouldIssueNewLease() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("old-refresh-token");
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("new-access")
                .build();

        when(refreshService.execute(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    @WithMockUser // Required so the Sentinel doesn't block the request at the filter level
    void logout_ShouldExileIdentity() throws Exception {
        mockMvc.perform(post("/api/auth/logout").with(csrf())
                        .header("Authorization", "Bearer mock-jwt"))
                .andExpect(status().isOk())
                .andExpect(content().string("Identity successfully exiled from the active session."));

        verify(authenticateService).logout("mock-jwt");
    }
}

