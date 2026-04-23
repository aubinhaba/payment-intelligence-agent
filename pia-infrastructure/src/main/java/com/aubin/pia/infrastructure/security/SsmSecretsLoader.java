package com.aubin.pia.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.SsmException;

/**
 * Loads sensitive parameters from AWS SSM Parameter Store at startup and injects them as system
 * properties. This avoids storing secrets in application.yml or environment variables.
 *
 * <p>Failures are non-fatal (warns and continues) so the app can start in degraded mode during
 * local development if LocalStack SSM is not seeded.
 */
@Component
public class SsmSecretsLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SsmSecretsLoader.class);

    private final SsmClient ssmClient;
    private final String claudeApiKeyPath;

    public SsmSecretsLoader(
            SsmClient ssmClient,
            @Value("${pia.ssm.claude-api-key-path:/pia/claude/api-key}") String claudeApiKeyPath) {
        this.ssmClient = ssmClient;
        this.claudeApiKeyPath = claudeApiKeyPath;
    }

    @Override
    public void run(ApplicationArguments args) {
        loadClaudeApiKey();
    }

    private void loadClaudeApiKey() {
        try {
            GetParameterResponse response =
                    ssmClient.getParameter(
                            GetParameterRequest.builder()
                                    .name(claudeApiKeyPath)
                                    .withDecryption(true)
                                    .build());
            System.setProperty("pia.claude.api-key", response.parameter().value());
            log.info("ssm.loaded parameter={}", claudeApiKeyPath);
        } catch (ParameterNotFoundException e) {
            log.warn(
                    "ssm.missing parameter={} — Claude agent will be unavailable",
                    claudeApiKeyPath);
        } catch (SsmException e) {
            log.warn(
                    "ssm.error parameter={} reason={} — Claude agent will be unavailable",
                    claudeApiKeyPath,
                    e.getMessage());
        }
    }
}
