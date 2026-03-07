package com.ranxom.zentry.services;

import com.ranxom.zentry.dto.RegisterRequest;
import com.ranxom.zentry.model.RefreshToken;
import com.ranxom.zentry.model.User;
import com.ranxom.zentry.repository.UserRepository;
import com.ranxom.zentry.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterServiceTest {

    @Mock private UserRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    // CHANGE THIS: Use @Mock, not @InjectMocks
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks private RegisterService registerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Arrange
        String username = "shizain";
        RegisterRequest request = new RegisterRequest(username, "shizain@zentry.io", "password123");

        when(repository.existsByUsername(username)).thenReturn(false);
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(jwtService.generateToken(any())).thenReturn("mocked_jwt");

        // Now that refreshTokenService is a MOCK, this line will work
        // without calling the real findByUsername() logic.
        RefreshToken mockRefreshToken = RefreshToken.builder().token("mock-uuid").build();
        when(refreshTokenService.createRefreshToken(username)).thenReturn(mockRefreshToken);

        // Act
        var response = registerService.execute(request);

        // Assert
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("mocked_jwt", response.getAccessToken());
        assertEquals("mock-uuid", response.getRefreshToken());
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("ranxom", "admin@zentry.io", "pass");
        when(repository.existsByUsername("ranxom")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> registerService.execute(request));
        assertEquals("Username already claimed within Zentry.", exception.getMessage());
        verify(repository, never()).save(any());
    }

}