package com.douglasavila.authservice.config;

import com.douglasavila.authservice.entity.User;
import com.douglasavila.authservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultUserConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        var user = userRepository.findByUsername("John");

        user.ifPresentOrElse(
                existingUser -> {
                    System.out.println("User " + existingUser.getUsername() + " already registered.");
                },
                () -> {
                    var newUser = new User();
                    newUser.setUsername("John");
                    newUser.setPassword(passwordEncoder.encode("123"));
                    userRepository.save(newUser);
                }
        );
    }
}
