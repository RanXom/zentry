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

import java.util.Date;

@Service
public class AuthenticateService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService blacklistService;

    public AuthenticateService(
            UserRepository repository,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService,
            TokenBlacklistService blacklistService
    ) {
        this.jwtService = jwtService;
        this.repository = repository;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.blacklistService = blacklistService;
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

    @Auditable(action = "IDENTITY_EXILED")
    public void logout(String token) {
        String jti = jwtService.extractJti(token);
        Date expiration = jwtService.extractExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();

        if (ttl > 0) {
            blacklistService.blacklistToken(jti, ttl);
        }

        // Nuke the Refresh Token from Postgres to kill the whole session
        String username = jwtService.extractUsername(token);
        var user = repository.findByUsername(username).orElseThrow();
        refreshTokenService.revokeRefreshToken(user);
    }

}
