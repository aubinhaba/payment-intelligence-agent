package com.aubin.pia.infrastructure.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * Provides AWS infrastructure beans not covered by Spring Cloud AWS auto-configuration.
 *
 * <p>When {@code pia.aws.ssm.endpoint} is set (local/test profile with LocalStack), a custom {@link
 * SsmClient} pointing to that endpoint is created. Otherwise the default AWS SDK credential/region
 * chain is used (prod).
 */
@Configuration
public class AwsInfraConfig {

    @Value("${spring.cloud.aws.region.static:eu-west-1}")
    private String awsRegion;

    @Bean
    @ConditionalOnProperty(name = "pia.aws.ssm.endpoint")
    public SsmClient localSsmClient(@Value("${pia.aws.ssm.endpoint}") String endpoint) {
        return SsmClient.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")))
                .region(Region.of(awsRegion))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(SsmClient.class)
    public SsmClient ssmClient() {
        return SsmClient.builder().region(Region.of(awsRegion)).build();
    }
}
