package com.ranxom.zentry.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public ResponseEntity<String> register() {
        return ResponseEntity.status(HttpStatus.OK).body("User Registered");
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticate() {
        return ResponseEntity.status(HttpStatus.OK).body("User Authenticated");
    }
}
