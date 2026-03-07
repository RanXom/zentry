package com.ranxom.zentry.controller;

import com.ranxom.zentry.security.ZentryUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal ZentryUserDetails userDetails) {
        // Extract the user directly from the Security Context
        var user = userDetails.user();

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "status", user.isActive() ? "ACTIVE" : "INACTIVE",
                "roles", user.getRoles()
        ));
    }
}