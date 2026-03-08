package com.ranxom.zentry.services;

import com.ranxom.zentry.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(StringRedisTemplate redisTemplate, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String RESET_PREFIX = "pwd_reset:";

    /**
     * Forges a unique token linked to an email, valid for 15 minutes.
     */
    public String createResetToken(String email) {
        // We check existence to avoid useless Redis entries,
        // but we'll always return a UUID to the controller to prevent enumeration.
        boolean exists = userRepository.existsByEmail(email);
        String token = UUID.randomUUID().toString();

        if (exists) {
            redisTemplate.opsForValue().set(
                    RESET_PREFIX + token,
                    email,
                    Duration.ofMinutes(15)
            );
            log.info("Restoration token forged for seeker: {}", email);
        } else {
            log.warn("Restoration attempted for non-existent email: {}", email);
        }

        return token;
    }

    /**
     * Validates the token and updates the identity's credentials.
     */
    public void completeReset(String token, String newPassword) {
        String email = redisTemplate.opsForValue().get(RESET_PREFIX + token);

        if (email == null) {
            log.error("Invalid or expired restoration token used: {}", token);
            throw new RuntimeException("The restoration link has expired or is invalid.");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Identity lost in the void."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Token is one-time use; burn it.
        redisTemplate.delete(RESET_PREFIX + token);
        log.info("Identity successfully restored for seeker: {}", email);
    }

}