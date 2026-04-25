package com.aubin.pia.infrastructure.agent.claude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeRequest;
import com.aubin.pia.infrastructure.agent.claude.dto.ClaudeResponse;
import com.aubin.pia.infrastructure.agent.claude.dto.ContentBlock;
import com.aubin.pia.infrastructure.agent.claude.dto.MessageDto;
import com.aubin.pia.infrastructure.agent.claude.dto.ToolDefinition;
import com.aubin.pia.infrastructure.agent.claude.tools.AgentTool;

public class ToolCallingLoop {

    private final ClaudeApiClient claudeApiClient;
    private final Map<String, AgentTool> tools;
    private final int maxIterations;
    private final ObjectMapper mapper;

    public ToolCallingLoop(
            ClaudeApiClient claudeApiClient,
            Map<String, AgentTool> tools,
            int maxIterations,
            ObjectMapper mapper) {
        this.claudeApiClient = claudeApiClient;
        this.tools = tools;
        this.maxIterations = maxIterations;
        this.mapper = mapper;
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

            if (response.isEndTurn()) {
                return parseReport(response.textContent());
            }

            messages.add(new MessageDto("assistant", response.getContent()));

            List<ContentBlock> toolResults = new ArrayList<>();
            for (ContentBlock toolUse : response.toolUseBlocks()) {
                String result = executeToolSafely(toolUse);
                toolResults.add(ContentBlock.toolResult(toolUse.getId(), result));
            }
            messages.add(new MessageDto("user", toolResults));
        }

        throw new AgentIterationLimitException(maxIterations);
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
            JsonNode node = mapper.readTree(textContent);
            String summary = node.required("summary").asText();
            String analysis = node.required("analysis").asText();
            return new ReportContent(summary, analysis);
        } catch (Exception e) {
            return new ReportContent("Analysis complete", textContent);
        }
    }
}
