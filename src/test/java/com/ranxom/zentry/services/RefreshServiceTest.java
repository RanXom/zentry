package com.ranxom.zentry.services;

import com.ranxom.zentry.model.RefreshToken;
import com.ranxom.zentry.model.User;
import com.ranxom.zentry.repository.RefreshTokenRepository;
import com.ranxom.zentry.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void shouldRefreshAccessToken() {
        // Arrange
        String tokenStr = "valid-uuid";
        User user = User.builder().username("shizain").build();
        RefreshToken refreshToken = RefreshToken.builder().token(tokenStr).user(user).build();

        when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn("new-access-token");

        // Act
        var response = refreshService.execute(tokenStr);

        // Assert
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals(tokenStr, response.getRefreshToken());
    }

}