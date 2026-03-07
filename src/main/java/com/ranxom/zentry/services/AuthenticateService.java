package com.ranxom.zentry.services;

import com.ranxom.zentry.aop.Auditable;
import com.ranxom.zentry.dto.AuthenticationRequest;
import com.ranxom.zentry.dto.AuthenticationResponse;
import com.ranxom.zentry.repository.UserRepository;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.security.ZentryUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthenticateService(
            UserRepository repository,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService
    ) {
        this.jwtService = jwtService;
        this.repository = repository;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    @Auditable(action = "IDENTITY_AUTHENTICATED")
    public AuthenticationResponse execute(AuthenticationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var user = repository.findByUsername(request.getUsername()).orElseThrow();

        user.setLastLogin(java.time.LocalDateTime.now());
        repository.save(user);

        var userDetails = new ZentryUserDetails(user);
        var accessToken = jwtService.generateToken(userDetails);
        var refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();

    }

}
