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
        // 1. REGISTER
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

        // 2. LOGIN
        AuthenticationRequest login = new AuthenticationRequest("integration_user", "Password123!");

        var loginResponse = client.post()
                .uri("/api/auth/login")
                .body(login)
                .exchangeSuccessfully()
                .expectBody(AuthenticationResponse.class)
                .returnResult()
                .getResponseBody();

        String accessToken = loginResponse.getAccessToken();

        // 3. ACCESS PROTECTED PROFILE
        client.get()
                .uri("/api/users/me")
                .headers(h -> h.setBearerAuth(accessToken))
                .exchangeSuccessfully()
                .expectBody()
                .jsonPath("$.username").isEqualTo("integration_user");
    }

}