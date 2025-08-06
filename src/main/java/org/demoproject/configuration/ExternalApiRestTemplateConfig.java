package org.demoproject.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestOperations;

import java.time.Duration;

@Configuration
@EnableRetry
public class ExternalApiRestTemplateConfig {

    @Bean
    public RestOperations restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(7))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }
}
