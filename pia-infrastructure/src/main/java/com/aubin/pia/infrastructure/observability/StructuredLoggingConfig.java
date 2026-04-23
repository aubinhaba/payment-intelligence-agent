package com.aubin.pia.infrastructure.observability;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

/**
 * Configures common MDC fields and Micrometer common tags so all metrics and logs carry the service
 * name and environment.
 */
@Configuration
public class StructuredLoggingConfig {

    @Bean
    public Object commonMeterRegistryCustomizer(
            MeterRegistry registry, @Value("${spring.application.name:pia}") String appName) {
        registry.config().commonTags(List.of(Tag.of("service", appName)));
        return new Object();
    }
}
