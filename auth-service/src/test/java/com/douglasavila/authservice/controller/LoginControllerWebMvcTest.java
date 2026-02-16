package com.douglasavila.authservice.controller;

import com.douglasavila.authservice.entity.User;
import com.douglasavila.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private static final String USERNAME = "Paul";
    private static final String PASSWORD = "password";
    private static final String TOKEN_VALUE = "jwt.token.value";

    private void expect401RequiredFields(String jsonBody) throws Exception {
        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("User and password are required"));

        verifyNoInteractions(userRepository, jwtEncoder, passwordEncoder);
    }

    private void expect401InvalidCreds(String jsonBody) throws Exception {
        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("User or password is invalid"));
    }

    @Test
    void login_returns401_whenUsernameIsMissing_null() throws Exception {
        expect401RequiredFields("""
            {"password":"%s"}
            """.formatted(PASSWORD));
    }

    @Test
    void login_returns401_whenUsernameIsBlank_emptyString() throws Exception {
        expect401RequiredFields("""
            {"username":"","password":"%s"}
            """.formatted(PASSWORD));
    }

    @Test
    void login_returns401_whenUsernameIsBlank_whitespace() throws Exception {
        expect401RequiredFields("""
            {"username":"   ","password":"%s"}
            """.formatted(PASSWORD));
    }

    @Test
    void login_returns401_whenPasswordIsMissing_null() throws Exception {
        expect401RequiredFields("""
            {"username":"%s"}
            """.formatted(USERNAME));
    }

    @Test
    void login_returns401_whenPasswordIsBlank_emptyString() throws Exception {
        expect401RequiredFields("""
            {"username":"%s","password":""}
            """.formatted(USERNAME));
    }

    @Test
    void login_returns401_whenPasswordIsBlank_whitespace() throws Exception {
        expect401RequiredFields("""
            {"username":"%s","password":"   "}
            """.formatted(USERNAME));
    }

    @Test
    void login_returns401_whenBothFieldsMissing_emptyObject() throws Exception {
        expect401RequiredFields("""
            {}
            """);
    }

    @Test
    void login_returns401_whenUserDoesNotExist() throws Exception {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        expect401InvalidCreds("""
            {"username":"%s","password":"%s"}
            """.formatted(USERNAME, PASSWORD));

        verify(userRepository).findByUsername(USERNAME);
        verifyNoInteractions(jwtEncoder, passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void login_returns401_whenPasswordIsInvalid() throws Exception {
        User user = mock(User.class);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(user.isLoginCorrect(any(), eq(passwordEncoder))).thenReturn(false);

        expect401InvalidCreds("""
            {"username":"%s","password":"%s"}
            """.formatted(USERNAME, PASSWORD));

        verify(userRepository).findByUsername(USERNAME);
        verify(user).isLoginCorrect(any(), eq(passwordEncoder));
        verifyNoInteractions(jwtEncoder);
        verifyNoMoreInteractions(userRepository, user);
    }

    @Test
    void login_returns200_withJwtAndExpiresIn_whenCredentialsAreValid() throws Exception {
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(user.isLoginCorrect(any(), eq(passwordEncoder))).thenReturn(true);
        when(user.getUserId()).thenReturn(userId);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new Jwt(
                        TOKEN_VALUE,
                        Instant.EPOCH,
                        Instant.EPOCH.plusSeconds(300),
                        Map.of("alg", "none"),
                        Map.of("sub", userId.toString())
                ));

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(TOKEN_VALUE))
                .andExpect(jsonPath("$.expiresIn").value(300));

        verify(userRepository).findByUsername(USERNAME);
        verify(user).isLoginCorrect(any(), eq(passwordEncoder));
        verify(user).getUserId();
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
        verifyNoMoreInteractions(userRepository, user, jwtEncoder);
    }

    @Test
    void login_encodesJwtWithIssuerAndSubjectAndExpiration() throws Exception {
        User user = mock(User.class);
        UUID userId = UUID.fromString("11111111-2222-3333-4444-555555555555");

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(user.isLoginCorrect(any(), eq(passwordEncoder))).thenReturn(true);
        when(user.getUserId()).thenReturn(userId);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(new Jwt(
                        TOKEN_VALUE,
                        Instant.EPOCH,
                        Instant.EPOCH.plusSeconds(300),
                        Map.of("alg", "none"),
                        Map.of("sub", userId.toString())
                ));

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"username":"%s","password":"%s"}
                            """.formatted(USERNAME, PASSWORD)))
                .andExpect(status().isOk());

        var captor = org.mockito.ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        org.junit.jupiter.api.Assertions.assertEquals("card-platform", claims.getClaim("iss"));
        org.junit.jupiter.api.Assertions.assertEquals(userId.toString(), claims.getSubject());

        Instant issuedAt = claims.getIssuedAt();
        Instant expiresAt = claims.getExpiresAt();

        org.junit.jupiter.api.Assertions.assertNotNull(issuedAt);
        org.junit.jupiter.api.Assertions.assertNotNull(expiresAt);
        org.junit.jupiter.api.Assertions.assertEquals(300L, Duration.between(issuedAt, expiresAt).getSeconds());
    }
}
