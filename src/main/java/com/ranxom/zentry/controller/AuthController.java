package com.ranxom.zentry.controller;

import com.ranxom.zentry.dto.AuthenticationRequest;
import com.ranxom.zentry.dto.AuthenticationResponse;
import com.ranxom.zentry.dto.RegisterRequest;
import com.ranxom.zentry.dto.TokenRefreshRequest;
import com.ranxom.zentry.model.RefreshToken;
import com.ranxom.zentry.repository.RefreshTokenRepository;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.security.ZentryUserDetails;
import com.ranxom.zentry.services.AuthenticateService;
import com.ranxom.zentry.services.RefreshService;
import com.ranxom.zentry.services.RefreshTokenService;
import com.ranxom.zentry.services.RegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenRepository refreshTokenRepository;

    private final AuthenticateService authenticateService;
    private final RegisterService registerService;
    private final RefreshService refreshService;

    public AuthController(
            RefreshTokenRepository refreshTokenRepository,
            AuthenticateService authenticateService,
            RegisterService registerService,
            RefreshService refreshService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;

        this.authenticateService = authenticateService;
        this.registerService = registerService;
        this.refreshService = refreshService;
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

}