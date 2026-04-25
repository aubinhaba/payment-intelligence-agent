package com.aubin.pia.infrastructure.agent.claude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeRequest;
import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeResponse;
import com.aubin.pia.infrastructure.agent.claude.dto.ContentBlock;
import com.aubin.pia.infrastructure.agent.claude.dto.MessageDto;
import com.aubin.pia.infrastructure.agent.claude.dto.UsageDto;
import com.aubin.pia.infrastructure.agent.claude.tools.AgentTool;

@ExtendWith(MockitoExtension.class)
class ToolCallingLoopTest {

    @Mock ClaudeApiClient claudeApiClient;
    @Mock AgentTool tool;

    private ToolCallingLoop loop;
    private ObjectMapper mapper;

    private static final String MODEL = "claude-sonnet-4-6";
    private static final int MAX_TOKENS = 1024;
    private static final int MAX_ITERATIONS = 5;
    private static final String SYSTEM_PROMPT = "You are a payment analyst.";

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        when(tool.name()).thenReturn("get_transaction_history");
        loop =
                new ToolCallingLoop(
                        claudeApiClient, Map.of(tool.name(), tool), MAX_ITERATIONS, mapper);
    }

    @Test
    void should_return_report_when_end_turn_on_first_call() {
        when(claudeApiClient.send(any(ClaudeRequest.class)))
                .thenReturn(endTurnResponse("High risk detected", "## Analysis\nDetails here."));

        ReportContent result =
                loop.run(
                        MODEL,
                        MAX_TOKENS,
                        SYSTEM_PROMPT,
                        List.of(new MessageDto("user", "Context")),
                        List.of());

        assertThat(result.summary()).isEqualTo("High risk detected");
        assertThat(result.markdownBody()).contains("Details here.");
    }

    @Test
    void should_call_tool_and_continue_when_tool_use_response() {
        when(claudeApiClient.send(any(ClaudeRequest.class)))
                .thenReturn(toolUseResponse("toolu_01", "get_transaction_history"))
                .thenReturn(endTurnResponse("Analysis done", "## Result\nAll clear."));
        when(tool.execute(any(JsonNode.class))).thenReturn("[{\"id\":\"tx_001\"}]");

        ReportContent result =
                loop.run(
                        MODEL,
                        MAX_TOKENS,
                        SYSTEM_PROMPT,
                        List.of(new MessageDto("user", "Context")),
                        List.of());

        verify(claudeApiClient, times(2)).send(any(ClaudeRequest.class));
        verify(tool).execute(any(JsonNode.class));
        assertThat(result.summary()).isEqualTo("Analysis done");
    }

    @Test
    void should_throw_when_max_iterations_exceeded() {
        when(claudeApiClient.send(any(ClaudeRequest.class)))
                .thenReturn(toolUseResponse("toolu_01", "get_transaction_history"));
        when(tool.execute(any(JsonNode.class))).thenReturn("{}");

        assertThatThrownBy(
                        () ->
                                loop.run(
                                        MODEL,
                                        MAX_TOKENS,
                                        SYSTEM_PROMPT,
                                        List.of(new MessageDto("user", "Context")),
                                        List.of()))
                .isInstanceOf(AgentIterationLimitException.class)
                .hasMessageContaining(String.valueOf(MAX_ITERATIONS));
    }

    @Test
    void should_handle_unknown_tool_gracefully_and_continue() {
        ClaudeResponse unknownToolResp = toolUseResponse("toolu_99", "unknown_tool");
        ClaudeResponse endTurnResp = endTurnResponse("Done", "## Done");
        when(claudeApiClient.send(any(ClaudeRequest.class)))
                .thenReturn(unknownToolResp)
                .thenReturn(endTurnResp);

        ReportContent result =
                loop.run(
                        MODEL,
                        MAX_TOKENS,
                        SYSTEM_PROMPT,
                        List.of(new MessageDto("user", "Context")),
                        List.of());

        verify(claudeApiClient, times(2)).send(any(ClaudeRequest.class));
        assertThat(result.summary()).isEqualTo("Done");
    }

    private ClaudeResponse endTurnResponse(String summary, String body) {
        String json =
                "{\"summary\":\""
                        + summary
                        + "\",\"analysis\":\""
                        + body.replace("\n", "\\n")
                        + "\"}";
        return new ClaudeResponse(
                "msg_01", "end_turn", List.of(ContentBlock.text(json)), new UsageDto(100, 50));
    }

    private ClaudeResponse toolUseResponse(String toolUseId, String toolName) {
        return new ClaudeResponse(
                "msg_02",
                "tool_use",
                List.of(ContentBlock.toolUse(toolUseId, toolName, mapper.createObjectNode())),
                new UsageDto(200, 80));
    }
}
