package com.aubin.pia.infrastructure.agent.claude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeRequest;
import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeResponse;
import com.aubin.pia.infrastructure.agent.claude.dto.MessageDto;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

class ClaudeApiClientTest {

    private MockWebServer mockWebServer;
    private ClaudeApiClient client;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        WebClient webClient =
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
        client = new ClaudeApiClient(webClient, "test-api-key", mapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void should_send_api_key_and_anthropic_version_headers() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody(endTurnResponseJson()));

        client.send(minimalRequest());

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getHeader("x-api-key")).isEqualTo("test-api-key");
        assertThat(recorded.getHeader("anthropic-version")).isNotBlank();
        assertThat(recorded.getPath()).isEqualTo("/v1/messages");
    }

    @Test
    void should_return_response_with_end_turn_stop_reason() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody(endTurnResponseJson()));

        ClaudeResponse response = client.send(minimalRequest());

        assertThat(response.isEndTurn()).isTrue();
        assertThat(response.textContent()).contains("Analysis complete");
    }

    @Test
    void should_return_response_with_tool_use_blocks() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody(toolUseResponseJson()));

        ClaudeResponse response = client.send(minimalRequest());

        assertThat(response.isToolUse()).isTrue();
        assertThat(response.toolUseBlocks()).hasSize(1);
        assertThat(response.toolUseBlocks().get(0).getName()).isEqualTo("get_transaction_history");
    }

    @Test
    void should_expose_usage_metrics() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody(endTurnResponseJson()));

        ClaudeResponse response = client.send(minimalRequest());

        assertThat(response.usage().inputTokens()).isEqualTo(100);
        assertThat(response.usage().outputTokens()).isEqualTo(50);
    }

    @Test
    void should_throw_on_4xx_response() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(401)
                        .addHeader("Content-Type", "application/json")
                        .setBody(
                                "{\"error\":{\"type\":\"authentication_error\","
                                        + "\"message\":\"Invalid API key\"}}"));

        assertThatThrownBy(() -> client.send(minimalRequest()))
                .isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void should_throw_on_5xx_response() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(500)
                        .addHeader("Content-Type", "application/json")
                        .setBody(
                                "{\"error\":{\"type\":\"api_error\","
                                        + "\"message\":\"Internal server error\"}}"));

        assertThatThrownBy(() -> client.send(minimalRequest()))
                .isInstanceOf(WebClientResponseException.class);
    }

    private ClaudeRequest minimalRequest() {
        return new ClaudeRequest(
                "claude-sonnet-4-6",
                1024,
                "System prompt",
                List.of(),
                List.of(new MessageDto("user", "Analyze this")));
    }

    private String endTurnResponseJson() {
        return """
               {
                 "id": "msg_01",
                 "type": "message",
                 "role": "assistant",
                 "content": [{"type": "text", "text": "Analysis complete. Risk: LOW."}],
                 "stop_reason": "end_turn",
                 "usage": {"input_tokens": 100, "output_tokens": 50}
               }
               """;
    }

    private String toolUseResponseJson() {
        return """
               {
                 "id": "msg_02",
                 "type": "message",
                 "role": "assistant",
                 "content": [
                   {
                     "type": "tool_use",
                     "id": "toolu_01",
                     "name": "get_transaction_history",
                     "input": {"card_reference_hash": "hash_001", "window_hours": 24}
                   }
                 ],
                 "stop_reason": "tool_use",
                 "usage": {"input_tokens": 200, "output_tokens": 80}
               }
               """;
    }
}
