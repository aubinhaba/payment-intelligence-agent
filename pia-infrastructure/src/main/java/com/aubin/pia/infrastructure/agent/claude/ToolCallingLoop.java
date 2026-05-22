package com.aubin.pia.infrastructure.agent.claude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.application.port.out.MetricsPublisher;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeRequest;
import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeResponse;
import com.aubin.pia.infrastructure.agent.claude.dto.ContentBlock;
import com.aubin.pia.infrastructure.agent.claude.dto.MessageDto;
import com.aubin.pia.infrastructure.agent.claude.dto.ToolDefinition;
import com.aubin.pia.infrastructure.agent.claude.dto.UsageDto;
import com.aubin.pia.infrastructure.agent.claude.tools.AgentTool;

public class ToolCallingLoop {

    private static final Logger log = LoggerFactory.getLogger(ToolCallingLoop.class);

    private final ClaudeApiClient claudeApiClient;
    private final Map<String, AgentTool> tools;
    private final int maxIterations;
    private final ObjectMapper mapper;
    private final MetricsPublisher metricsPublisher;

    @SuppressWarnings({"EI_EXPOSE_REP2"}) // ObjectMapper is a thread-safe singleton
    public ToolCallingLoop(
            ClaudeApiClient claudeApiClient,
            Map<String, AgentTool> tools,
            int maxIterations,
            ObjectMapper mapper,
            MetricsPublisher metricsPublisher) {
        this.claudeApiClient = claudeApiClient;
        this.tools = Map.copyOf(tools);
        this.maxIterations = maxIterations;
        this.mapper = mapper; // ObjectMapper is a thread-safe singleton — no defensive copy needed
        this.metricsPublisher = metricsPublisher;
    }

    public ReportContent run(
            String model,
            int maxTokens,
            String systemPrompt,
            List<MessageDto> initialMessages,
            List<ToolDefinition> toolDefinitions) {

        List<MessageDto> messages = new ArrayList<>(initialMessages);

        for (int i = 0; i < maxIterations; i++) {
            ClaudeRequest request =
                    new ClaudeRequest(model, maxTokens, systemPrompt, toolDefinitions, messages);
            ClaudeResponse response = claudeApiClient.send(request);

            recordTokens(response.usage());

            if (response.isEndTurn()) {
                return parseReport(response.textContent());
            }

            if (response.isMaxTokens()) {
                log.warn("claude.response.truncated — returning partial content as-is");
                return parseReport(response.textContent());
            }

            messages.add(new MessageDto("assistant", response.getContent()));

            List<ContentBlock> toolResults = new ArrayList<>();
            for (ContentBlock toolUse : response.toolUseBlocks()) {
                String result = executeToolSafely(toolUse);
                toolResults.add(ContentBlock.toolResult(toolUse.getId(), result));
            }
            if (!toolResults.isEmpty()) {
                messages.add(new MessageDto("user", toolResults));
            }
        }

        throw new AgentIterationLimitException(maxIterations);
    }

    private void recordTokens(UsageDto usage) {
        if (usage != null) {
            metricsPublisher.recordAgentTokensUsed(usage.inputTokens(), usage.outputTokens());
        }
    }

    private String executeToolSafely(ContentBlock toolUse) {
        AgentTool tool = tools.get(toolUse.getName());
        if (tool == null) {
            return "{\"error\":\"Unknown tool: " + toolUse.getName() + "\"}";
        }
        JsonNode input =
                toolUse.getInput() != null ? toolUse.getInput() : mapper.createObjectNode();
        return tool.execute(input);
    }

    private ReportContent parseReport(String textContent) {
        try {
            JsonNode node = mapper.readTree(stripCodeFence(textContent));
            String summary = node.required("summary").asText();
            String analysis = node.required("analysis").asText();
            return new ReportContent(summary, analysis);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return new ReportContent("Analysis complete", textContent);
        }
    }

    /**
     * Strips a leading ```json (or ```) / trailing ``` fence that Claude sometimes wraps around
     * JSON.
     */
    private static String stripCodeFence(String text) {
        String t = text.strip();
        if (!t.startsWith("```")) {
            return t;
        }
        int firstNewline = t.indexOf('\n');
        if (firstNewline < 0) {
            return t;
        }
        String body = t.substring(firstNewline + 1);
        if (body.endsWith("```")) {
            body = body.substring(0, body.length() - 3);
        }
        return body.strip();
    }
}
