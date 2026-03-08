package com.ranxom.zentry.services;

import com.ranxom.zentry.dto.AdminUserUpdate;
import com.ranxom.zentry.dto.UserResponse;
import com.ranxom.zentry.model.Role;
import com.ranxom.zentry.model.User;
import com.ranxom.zentry.repository.RoleRepository;
import com.ranxom.zentry.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUser(Long id, AdminUserUpdate update) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Identity not found in the vault."));

        if (update.getUsername() != null) user.setUsername(update.getUsername());
        if (update.getEmail() != null) user.setEmail(update.getEmail());
        if (update.getIsActive() != null) user.setActive(update.getIsActive());
        if (update.getAccountLocked() != null) user.setAccountLocked(update.getAccountLocked());

        if (update.getPassword() != null && !update.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(update.getPassword()));
        }

        userRepository.save(user);
    }

    @Transactional
    public void toggleLock(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAccountLocked(!user.isAccountLocked());
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername() != null ? user.getUsername() : "Unknown")
                .email(user.getEmail() != null ? user.getEmail() : "No Email")
                .isActive(user.isActive())
                .accountLocked(user.isAccountLocked())
                .roles(user.getRoles() != null ?
                        user.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet()) :
                        Set.of())
                .build();
    }

}
