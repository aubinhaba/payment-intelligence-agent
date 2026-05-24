package com.aubin.pia.simulator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfig {

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient, ObjectMapper objectMapper) {
        SqsMessagingMessageConverter converter = new SqsMessagingMessageConverter();
        // Use the application ObjectMapper (JavaTimeModule registered) so Instant serialises
        // correctly.
        converter.setObjectMapper(objectMapper);
        // Do not emit the JavaType MessageAttribute — the consumer deserialises
        // to its own DTO type and must not be coupled to the simulator's class names.
        converter.setPayloadTypeHeaderValueFunction(msg -> null);
        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .messageConverter(converter)
                .build();
    }
}
