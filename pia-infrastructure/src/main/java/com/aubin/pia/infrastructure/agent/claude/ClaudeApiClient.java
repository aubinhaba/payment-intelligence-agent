package com.aubin.pia.infrastructure.agent.claude;

import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeRequest;
import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeResponse;

public class ClaudeApiClient {

    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper mapper;

    public ClaudeApiClient(WebClient webClient, String apiKey, ObjectMapper mapper) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.mapper = mapper;
    }

    public ClaudeResponse send(ClaudeRequest request) {
        return webClient
                .post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .header("content-type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponse.class)
                .block();
    }
}
