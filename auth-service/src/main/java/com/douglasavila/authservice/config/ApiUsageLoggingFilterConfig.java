package com.douglasavila.authservice.config;

import com.douglasavila.authservice.infrastructure.web.filter.ApiUsageLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiUsageLoggingFilterConfig {

    @Bean
    ApiUsageLoggingFilter apiUsageLoggingFilter() {
        return new ApiUsageLoggingFilter();
    }
}
