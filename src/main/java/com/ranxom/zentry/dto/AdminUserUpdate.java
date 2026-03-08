package com.ranxom.zentry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserUpdate {

    private String username;
    private String email;
    private String password;
    private Boolean isActive;
    private Boolean accountLocked;

}
