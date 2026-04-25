package com.aubin.pia.infrastructure.agent.claude.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ClaudeRequest {

    @JsonProperty("model")
    private final String model;

    @JsonProperty("max_tokens")
    private final int maxTokens;

    @JsonProperty("system")
    private final String system;

    @JsonProperty("tools")
    private final List<ToolDefinition> tools;

    @JsonProperty("messages")
    private final List<MessageDto> messages;

    public ClaudeRequest(
            String model,
            int maxTokens,
            String system,
            List<ToolDefinition> tools,
            List<MessageDto> messages) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.system = system;
        this.tools = tools;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public String getSystem() {
        return system;
    }

    public List<ToolDefinition> getTools() {
        return tools;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }
}
