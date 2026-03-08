package com.ranxom.zentry.controller;

import com.ranxom.zentry.model.User;
import com.ranxom.zentry.security.ZentryUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest extends BaseControllerTest {

    @Test
    void getMyProfile_ShouldReturnMetadata() throws Exception {
        User mockUser = User.builder()
                .username("shizain_dev")
                .email("shizain@zentry.io")
                .active(true)
                .roles(Set.of())
                .build();

        ZentryUserDetails customDetails = new ZentryUserDetails(mockUser);

        mockMvc.perform(get("/api/users/me")
                        .with(user(customDetails))) // This bypasses @WithMockUser
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("shizain_dev"))
                .andExpect(jsonPath("$.email").value("shizain@zentry.io"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "SYSTEM_READ"})
    void adminCheck_ShouldAllowSovereigns() throws Exception {
        mockMvc.perform(get("/api/users/admin-only"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"}) // Standard user authority only
    void adminCheck_ShouldForbiddenStandardUsers() throws Exception {
        mockMvc.perform(get("/api/users/admin-only"))
                .andExpect(status().isForbidden());
    }

}
