package com.aubin.pia.infrastructure.agent.claude;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeRequest;
import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeResponse;

import reactor.core.publisher.Mono;

public class ClaudeApiClient {

    private static final Logger log = LoggerFactory.getLogger(ClaudeApiClient.class);
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper mapper;

    @SuppressWarnings({"EI_EXPOSE_REP2"}) // WebClient and ObjectMapper are thread-safe singletons
    public ClaudeApiClient(WebClient webClient, String apiKey, ObjectMapper mapper) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.mapper = mapper;
    }

    public ClaudeResponse send(ClaudeRequest request) {
        logRequest(request);
        return webClient
                .post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .header("content-type", "application/json")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus.BAD_REQUEST::equals, this::handleErrorResponse)
                .onStatus(HttpStatus.UNAUTHORIZED::equals, this::handleErrorResponse)
                .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals, this::handleErrorResponse)
                .onStatus(status -> status.is5xxServerError(), this::handleErrorResponse)
                .bodyToMono(ClaudeResponse.class)
                .block();
    }

    private Mono<? extends Throwable> handleErrorResponse(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("<empty body>")
                .map(
                        body -> {
                            log.error(
                                    "claude.api.error status={} body={}",
                                    response.statusCode().value(),
                                    body);
                            return WebClientResponseException.create(
                                    response.statusCode().value(),
                                    response.statusCode().toString(),
                                    response.headers().asHttpHeaders(),
                                    body.getBytes(),
                                    null);
                        });
    }

    private void logRequest(ClaudeRequest request) {
        if (log.isDebugEnabled()) {
            try {
                log.debug("claude.api.request body={}", mapper.writeValueAsString(request));
            } catch (JsonProcessingException e) {
                log.debug("claude.api.request body=<serialization-failed>");
            }
        }
    }
}
