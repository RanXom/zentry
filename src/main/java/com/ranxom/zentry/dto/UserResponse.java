package com.ranxom.zentry.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private boolean isActive;
    private boolean accountLocked;
    private Set<String> roles; // Just the names, like ["ROLE_USER", "ROLE_ADMIN"]

}