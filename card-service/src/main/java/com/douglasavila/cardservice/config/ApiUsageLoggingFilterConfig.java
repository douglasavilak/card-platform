package com.douglasavila.cardservice.config;


import com.douglasavila.cardservice.infrastructure.web.filter.ApiUsageLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiUsageLoggingFilterConfig {

    @Bean
    ApiUsageLoggingFilter apiUsageLoggingFilter() {
        return new ApiUsageLoggingFilter();
    }
}
