package com.ranxom.zentry.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public void blacklistToken(String jti, long expirationMillis) {
        redisTemplate.opsForValue().set(jti, "blacklisted",
                Duration.ofMillis(expirationMillis));
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(jti));
    }

}
