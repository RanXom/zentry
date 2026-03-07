package com.ranxom.zentry.services;

import com.ranxom.zentry.dto.AuthenticationResponse;
import com.ranxom.zentry.model.RefreshToken;
import com.ranxom.zentry.repository.RefreshTokenRepository;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.security.ZentryUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthenticationResponse execute(String requestToken) {
        return refreshTokenRepository.findByToken(requestToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Generate a fresh Access Token
                    String accessToken = jwtService.generateToken(new ZentryUserDetails(user));

                    return AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(requestToken)
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in the vault!"));
    }

}