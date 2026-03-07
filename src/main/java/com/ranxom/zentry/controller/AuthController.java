package com.ranxom.zentry.controller;

import com.ranxom.zentry.dto.AuthenticationRequest;
import com.ranxom.zentry.dto.AuthenticationResponse;
import com.ranxom.zentry.dto.RegisterRequest;
import com.ranxom.zentry.services.AuthenticateService;
import com.ranxom.zentry.services.RegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticateService authenticateService;
    private final RegisterService registerService;

    public AuthController(
            AuthenticateService authenticateService,
            RegisterService registerService
    ) {
        this.authenticateService = authenticateService;
        this.registerService = registerService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(registerService.execute(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authenticateService.execute(authenticationRequest));
    }

}