package com.ranxom.zentry.services;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    @Test
    void shouldAllowFiveRequestsAndBlockTheSixth() {
        String ip = "192.168.1.1";
        Bucket bucket = rateLimiterService.resolveBucket(ip);

        // First 5 tokens should be consumed successfully
        for (int i = 0; i < 5; i++) {
            assertTrue(bucket.tryConsume(1), "Request " + (i+1) + " should be allowed");
        }

        // The 6th token should be rejected
        assertFalse(bucket.tryConsume(1), "The 6th request should be rate-limited");
    }

    @Test
    void shouldHaveSeparateBucketsForDifferentIps() {
        String ip1 = "1.1.1.1";
        String ip2 = "2.2.2.2";

        Bucket bucket1 = rateLimiterService.resolveBucket(ip1);
        Bucket bucket2 = rateLimiterService.resolveBucket(ip2);

        // Consume all tokens for IP 1
        for (int i = 0; i < 5; i++) bucket1.tryConsume(1);

        // IP 1 should be blocked, but IP 2 should still be free
        assertFalse(bucket1.tryConsume(1));
        assertTrue(bucket2.tryConsume(1), "IP 2 should not be affected by IP 1's spam");
    }
}