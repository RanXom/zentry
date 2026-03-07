package com.ranxom.zentry.integration;

import com.ranxom.zentry.dto.AuthenticationRequest;
import com.ranxom.zentry.dto.AuthenticationResponse;
import com.ranxom.zentry.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthIntegrationTest {

    @Autowired
    private RestTestClient client; // RestTestClient instead of TestRestTemplate

    @Test
    void fullIdentityLifecycle_ShouldSucceed() {
        // REGISTER
        RegisterRequest reg = new RegisterRequest("integration_user", "int@zentry.io", "Password123!");

        var regResponse = client.post()
                .uri("/api/auth/register")
                .body(reg)
                .exchangeSuccessfully() // Checks for 2xx status automatically
                .expectBody(AuthenticationResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(regResponse).isNotNull();
        assertThat(regResponse.getRefreshToken()).isNotNull();

        // LOGIN
        AuthenticationRequest login = new AuthenticationRequest("integration_user", "Password123!");

        var loginResponse = client.post()
                .uri("/api/auth/login")
                .body(login)
                .exchangeSuccessfully()
                .expectBody(AuthenticationResponse.class)
                .returnResult()
                .getResponseBody();

        String accessToken = loginResponse.getAccessToken();

        // ACCESS PROTECTED PROFILE
        client.get()
                .uri("/api/users/me")
                .headers(h -> h.setBearerAuth(accessToken))
                .exchangeSuccessfully()
                .expectBody()
                .jsonPath("$.username").isEqualTo("integration_user");
    }
    @Test
    void logout_ShouldDropTheHammer() {
        // 1. CREATE A DYNAMIC IDENTITY (Programmatic Registration)
        String testUser = "user_" + System.currentTimeMillis(); // Unique per run
        RegisterRequest reg = new RegisterRequest(testUser, testUser + "@zentry.io", "Password123!");

        client.post().uri("/api/auth/register")
                .body(reg)
                .exchangeSuccessfully();

        // 2. LOGIN with the freshly minted credentials
        AuthenticationRequest login = new AuthenticationRequest(testUser, "Password123!");
        var loginRes = client.post().uri("/api/auth/login")
                .body(login)
                .exchangeSuccessfully()
                .expectBody(AuthenticationResponse.class)
                .returnResult()
                .getResponseBody();

        String token = loginRes.getAccessToken();

        // 3. VERIFY the token works initially
        client.get().uri("/api/users/me")
                .headers(h -> h.setBearerAuth(token))
                .exchangeSuccessfully();

        // 4. TRIGGER THE KILL SWITCH (Logout)
        client.post().uri("/api/auth/logout")
                .headers(h -> h.setBearerAuth(token))
                .exchangeSuccessfully();

        // 5. THE ULTIMATE JUDGMENT: TRY ACCESS AGAIN - Should be 401
        client.get().uri("/api/users/me")
                .headers(h -> h.setBearerAuth(token))
                .exchange()
                .expectStatus().isUnauthorized();
    }

}