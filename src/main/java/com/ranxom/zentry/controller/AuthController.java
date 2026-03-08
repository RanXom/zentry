package com.ranxom.zentry.controller;

import com.ranxom.zentry.dto.*;
import com.ranxom.zentry.model.RefreshToken;
import com.ranxom.zentry.repository.RefreshTokenRepository;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.security.ZentryUserDetails;
import com.ranxom.zentry.services.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticateService authenticateService;
    private final RegisterService registerService;
    private final RefreshService refreshService;
    private final PasswordResetService resetService;

    public AuthController(
            AuthenticateService authenticateService,
            RegisterService registerService,
            RefreshService refreshService,
            PasswordResetService resetService) {
        this.authenticateService = authenticateService;
        this.registerService = registerService;
        this.refreshService = refreshService;
        this.resetService = resetService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(registerService.execute(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authenticateService.execute(authenticationRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(refreshService.execute(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            authenticateService.logout(jwt);
        }
        return ResponseEntity.ok("Identity successfully exiled from the active session.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = resetService.createResetToken(request.email());

        log.info("SENTINEL_RESTORE_LINK: http://localhost:8080/api/auth/reset-password?token={}", token);
        return ResponseEntity.ok("If an identity is linked to this email, a restoration link has been forged.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @Valid @RequestBody ResetPasswordRequest request) {
        resetService.completeReset(token, request.newPassword());
        return ResponseEntity.ok("Identity has been successfully reshaped with new credentials.");
    }

}