package com.douglasavila.authservice.entity;

import com.douglasavila.authservice.controller.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTest {

    @Test
    void isLoginCorrect_returnsTrue_whenPasswordEncoderMatches() {
        User user = new User();
        user.setPassword("encoded");

        LoginRequest req = new LoginRequest("Paul", "plain");

        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.matches("plain", "encoded")).thenReturn(true);

        assertTrue(user.isLoginCorrect(req, encoder));

        verify(encoder).matches("plain", "encoded");
        verifyNoMoreInteractions(encoder);
    }

    @Test
    void isLoginCorrect_returnsFalse_whenPasswordEncoderDoesNotMatch() {
        User user = new User();
        user.setPassword("encoded");

        LoginRequest req = new LoginRequest("Paul", "plain");

        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.matches("plain", "encoded")).thenReturn(false);

        assertFalse(user.isLoginCorrect(req, encoder));

        verify(encoder).matches("plain", "encoded");
        verifyNoMoreInteractions(encoder);
    }

    @Test
    void gettersAndSetters_workAsExpected() {
        User user = new User();
        UUID id = UUID.randomUUID();

        user.setUserId(id);
        user.setUsername("Paul");
        user.setPassword("pw");

        assertEquals(id, user.getUserId());
        assertEquals("Paul", user.getUsername());
        assertEquals("pw", user.getPassword());
    }
}
