package com.aubin.pia.infrastructure.security;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.SsmException;

/**
 * Loads secrets from AWS SSM Parameter Store into the Spring {@link ConfigurableEnvironment} before
 * any bean is created, so {@code @Value} injections receive the real secret values.
 *
 * <p>Registered via {@code META-INF/spring.factories} and {@code
 * META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports}. Failures are
 * non-fatal: the app starts in degraded mode with the placeholder value.
 */
public class SsmPropertySourceLoader implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(SsmPropertySourceLoader.class);

    private static final String PROPERTY_SOURCE_NAME = "ssm-secrets";
    private static final String DEFAULT_KEY_PATH = "/pia/claude/api-key";
    private static final String DEFAULT_REGION = "eu-west-1";

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment, SpringApplication application) {
        String endpoint = environment.getProperty("pia.aws.ssm.endpoint");
        String keyPath = environment.getProperty("pia.ssm.claude-api-key-path", DEFAULT_KEY_PATH);
        String region = environment.getProperty("spring.cloud.aws.region.static", DEFAULT_REGION);

        try (SsmClient ssm = buildClient(endpoint, region)) {
            boolean decrypt = endpoint == null || endpoint.isBlank();
            String value =
                    ssm.getParameter(
                                    GetParameterRequest.builder()
                                            .name(keyPath)
                                            .withDecryption(decrypt)
                                            .build())
                            .parameter()
                            .value();

            environment
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource(
                                    PROPERTY_SOURCE_NAME, Map.of("pia.claude.api-key", value)));

            log.info("ssm.loaded parameter={}", keyPath);
        } catch (SsmException e) {
            log.warn(
                    "ssm.unavailable parameter={} reason={} — starting with placeholder key",
                    keyPath,
                    e.getMessage());
        } catch (Exception e) {
            log.warn(
                    "ssm.error parameter={} reason={} — starting with placeholder key",
                    keyPath,
                    e.getMessage());
        }
    }

    private SsmClient buildClient(String endpoint, String region) {
        SsmClientBuilder builder = SsmClient.builder().region(Region.of(region));
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create("test", "test")));
        }
        return builder.build();
    }
}
