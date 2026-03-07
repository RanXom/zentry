package com.ranxom.zentry.services;

import com.ranxom.zentry.aop.Auditable;
import com.ranxom.zentry.dto.AuthenticationRequest;
import com.ranxom.zentry.dto.AuthenticationResponse;
import com.ranxom.zentry.repository.UserRepository;
import com.ranxom.zentry.security.JwtService;
import com.ranxom.zentry.security.ZentryUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticateService(
            UserRepository repository,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.jwtService = jwtService;
        this.repository = repository;
        this.authenticationManager = authenticationManager;
    }

    @Auditable(action = "IDENTITY_AUTHENTICATED")
    public AuthenticationResponse execute(AuthenticationRequest request) {
        // This triggers the full Spring Security Auth Provider check
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = repository.findByUsername(request.getUsername())
                .orElseThrow();
        var userDetails = new ZentryUserDetails(user);
        var jwtToken = jwtService.generateToken(userDetails);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

}
