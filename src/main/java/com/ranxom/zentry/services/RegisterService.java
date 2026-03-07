package com.ranxom.zentry.services;

import com.ranxom.zentry.aop.Auditable;
import com.ranxom.zentry.dto.AuthenticationResponse;
import com.ranxom.zentry.dto.RegisterRequest;
import com.ranxom.zentry.model.User;
import com.ranxom.zentry.repository.UserRepository;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.security.ZentryUserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterService(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Auditable(action = "IDENTITY_REGISTERED")
    public AuthenticationResponse execute(RegisterRequest request) {

        if (repository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already claimed within Zentry.");
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already associated with an identity.");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypting
                .active(true)
                .build();
        repository.save(user);

        var userDetails = new ZentryUserDetails(user);
        var jwtToken = jwtService.generateToken(userDetails);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

}
