package com.ranxom.zentry.services;

import com.ranxom.zentry.dto.AuthenticationResponse;
import com.ranxom.zentry.model.RefreshToken;
import com.ranxom.zentry.model.User;
import com.ranxom.zentry.repository.RefreshTokenRepository;
import com.ranxom.zentry.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private JwtService jwtService;

    @InjectMocks private RefreshService refreshService;

    @Test
    void shouldRefreshSuccessfully() {
        // Arrange
        String rawToken = "valid-uuid-string";
        User user = User.builder().username("shizain").build();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(rawToken)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(60))
                .build();

        when(refreshTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn("new-access-token");

        // Act
        AuthenticationResponse response = refreshService.execute(rawToken);

        // Assert
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(rawToken, response.getRefreshToken());
        verify(jwtService, times(1)).generateToken(any());
    }

    @Test
    void shouldThrowExceptionWhenTokenNotFound() {
        // Arrange
        String invalidToken = "fake-token";
        when(refreshTokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> refreshService.execute(invalidToken));

        assertTrue(exception.getMessage().contains("not recognized"));
        verify(jwtService, never()).generateToken(any());
    }

}