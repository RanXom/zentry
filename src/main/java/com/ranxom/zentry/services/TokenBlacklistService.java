package com.ranxom.zentry.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public void blacklistToken(String jti, long ttl) {
        // Use a consistent prefix so the Filter can find it
        redisTemplate.opsForValue().set("BLACKLIST_" + jti, "true", Duration.ofMillis(ttl));
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey("BLACKLIST_" + jti));
    }

}
