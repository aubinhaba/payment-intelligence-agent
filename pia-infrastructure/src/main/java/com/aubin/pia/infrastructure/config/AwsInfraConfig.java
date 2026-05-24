package com.aubin.pia.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * Provides AWS infrastructure beans not covered by Spring Cloud AWS auto-configuration.
 *
 * <p>SSM secret loading is handled by {@link
 * com.aubin.pia.infrastructure.security.SsmPropertySourceLoader} (EnvironmentPostProcessor) which
 * runs before bean creation and builds its own short-lived SsmClient. This bean is retained for any
 * future use of SsmClient in application code.
 *
 * <p>The SQS listener factory is configured with {@code payloadTypeHeaderUsed = false} to prevent
 * deserialization failures when the simulator sends messages with a {@code JavaType} header
 * referencing its own internal DTO class, which is not on the bootstrap classpath.
 */
@Configuration
public class AwsInfraConfig {

    @Value("${spring.cloud.aws.region.static:eu-west-1}")
    private String awsRegion;

    @Bean
    public SsmClient ssmClient() {
        return SsmClient.builder().region(Region.of(awsRegion)).build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        SqsMessagingMessageConverter converter = new SqsMessagingMessageConverter();
        // Use the application ObjectMapper (JavaTimeModule registered) for correct Instant
        // handling.
        converter.setObjectMapper(objectMapper);
        // Ignore the JavaType MessageAttribute set by the simulator. When the mapper returns null,
        // Spring Cloud AWS falls back to the @SqsListener method's parameter type via context.
        converter.setPayloadTypeMapper(msg -> null);
        return SqsMessageListenerContainerFactory.builder()
                .configure(options -> options.messageConverter(converter))
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
}
