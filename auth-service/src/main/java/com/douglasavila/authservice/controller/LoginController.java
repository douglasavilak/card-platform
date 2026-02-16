package com.douglasavila.authservice.controller;

import com.douglasavila.authservice.controller.dto.ApiErrorResponse;
import com.douglasavila.authservice.controller.dto.LoginRequest;
import com.douglasavila.authservice.controller.dto.LoginResponse;
import com.douglasavila.authservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class LoginController {
    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginController(JwtEncoder jwtEncoder, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        if (loginRequest.username() == null || loginRequest.username().trim().isEmpty()
        || loginRequest.password() == null || loginRequest.password().trim().isEmpty()) {
            var body = new ApiErrorResponse(
                    401,
                    "Unauthorized",
                    "User and password are required"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(body);
        }

        var user = userRepository.findByUsername(loginRequest.username());

        if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, passwordEncoder)) {
            var body = new ApiErrorResponse(
                    401,
                    "Unauthorized",
                    "User or password is invalid"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(body);
        }

        var now = Instant.now();
        var expiresIn = 300L;

        var claims = JwtClaimsSet.builder()
                .issuer("card-platform")
                .subject(user.get().getUserId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return ResponseEntity.ok(new LoginResponse(jwtValue, expiresIn));

    }
}
