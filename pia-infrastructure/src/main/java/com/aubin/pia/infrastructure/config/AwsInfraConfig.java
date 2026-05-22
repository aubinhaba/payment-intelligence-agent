package com.aubin.pia.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * Provides AWS infrastructure beans not covered by Spring Cloud AWS auto-configuration.
 *
 * <p>SSM secret loading is handled by {@link
 * com.aubin.pia.infrastructure.security.SsmPropertySourceLoader} (EnvironmentPostProcessor) which
 * runs before bean creation and builds its own short-lived SsmClient. This bean is retained for any
 * future use of SsmClient in application code.
 */
@Configuration
public class AwsInfraConfig {

    @Value("${spring.cloud.aws.region.static:eu-west-1}")
    private String awsRegion;

    @Bean
    public SsmClient ssmClient() {
        return SsmClient.builder().region(Region.of(awsRegion)).build();
    }
}
