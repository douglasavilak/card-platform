package com.douglasavila.cardservice.config;

import com.douglasavila.cardservice.repository.CardStatusRepository;
import com.douglasavila.cardservice.util.BatchParser;
import com.douglasavila.cardservice.util.CardHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchParserConfig {

    @Bean
    public BatchParser batchParser(
            CardHasher hasher,
            CardStatusRepository cardStatusRepository
    ) {
        return new BatchParser(hasher, cardStatusRepository);
    }
}
