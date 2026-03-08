package com.ranxom.zentry.security;

import com.ranxom.zentry.services.RateLimiterService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    public RateLimitFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();

        try {
            Bucket bucket = rateLimiterService.resolveBucket(clientIp);

            if (bucket == null) {
                log.error("SENTINEL_ALERT: RateLimiterService returned null for IP: {}. Bypassing check.", clientIp);
                filterChain.doFilter(request, response);
                return;
            }

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                log.warn("RATE_LIMIT_EXCEEDED: IP {} has hit the threshold.", clientIp);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests. Slow your pace, seeker.");
            }
        } catch (Exception e) {
            log.error("SENTINEL_ERROR: Rate limiting failed unexpectedly: {}. Bypassing check.", e.getMessage());
            filterChain.doFilter(request, response);
        }

    }

}
