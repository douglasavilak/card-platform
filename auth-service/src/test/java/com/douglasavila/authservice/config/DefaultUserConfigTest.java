package com.douglasavila.authservice.config;

import com.douglasavila.authservice.entity.User;
import com.douglasavila.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultUserConfigTest {

    @Test
    void run_createsDefaultUser_whenUserDoesNotExist() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(userRepository.findByUsername("John")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        DefaultUserConfig config = new DefaultUserConfig(userRepository, passwordEncoder);

        config.run();

        verify(userRepository).findByUsername("John");
        verify(passwordEncoder).encode("123");
        verify(userRepository).save(any(User.class));
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }
}
