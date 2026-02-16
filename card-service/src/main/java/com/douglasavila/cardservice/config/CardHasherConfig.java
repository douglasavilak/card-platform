package com.douglasavila.cardservice.config;

import com.douglasavila.cardservice.util.CardHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CardHasherConfig {

    @Bean
    public CardHasher cardHasher(@Value("${card.hash.salt}") String salt) {
        return new CardHasher(salt);
    }
}
