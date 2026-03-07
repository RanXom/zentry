package com.ranxom.zentry.services;

import com.ranxom.zentry.model.RefreshToken;
import com.ranxom.zentry.model.User;
import com.ranxom.zentry.repository.RefreshTokenRepository;
import com.ranxom.zentry.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${zentry.security.jwt.refresh-expiration}")
    private Long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private final StringRedisTemplate redisTemplate;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();

        // Rotation: Remove old tokens to prevent session bloat
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please sign in again.");
        }
        return token;
    }

    public void blacklistToken(String jti, long ttl) {
        redisTemplate.opsForValue().set("BLACKLIST_" + jti, "true", Duration.ofMillis(ttl));
    }

    @Transactional
    public void revokeRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

}