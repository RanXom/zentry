package com.ranxom.zentry.services;

import com.ranxom.zentry.aop.Auditable;
import com.ranxom.zentry.dto.AuthenticationResponse;
import com.ranxom.zentry.dto.RegisterRequest;
import com.ranxom.zentry.model.Role;
import com.ranxom.zentry.model.User;
import com.ranxom.zentry.repository.RoleRepository;
import com.ranxom.zentry.repository.UserRepository;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.security.ZentryUserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public RegisterService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Auditable(action = "IDENTITY_REGISTERED")
    public AuthenticationResponse execute(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already claimed within Zentry.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already associated with an identity.");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("System Error: Default Role not found."));

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypting
                .active(true)
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);

        var userDetails = new ZentryUserDetails(user);
        var jwtToken = jwtService.generateToken(userDetails);

        var refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();

    }

}
