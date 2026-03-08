package com.ranxom.zentry.security;

import com.ranxom.zentry.services.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only care about rate limiting sensitive endpoints like Login and Register
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            String clientIp = request.getRemoteAddr();
            var bucket = rateLimiterService.resolveBucket(clientIp);

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Sentinel Alert: Too many requests. Try again in a minute.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
