package com.aubin.pia.infrastructure.agent.claude.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents one block in Claude's message content array. The {@code type} field discriminates
 * between text, tool_use (Claude invoking a tool), and tool_result (our response to a tool call).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ContentBlock {

    @JsonProperty("type")
    private final String type;

    @JsonProperty("text")
    private final String text;

    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("input")
    private final JsonNode input;

    @JsonProperty("tool_use_id")
    private final String toolUseId;

    @JsonProperty("content")
    private final String resultContent;

    @JsonCreator
    public ContentBlock(
            @JsonProperty("type") String type,
            @JsonProperty("text") String text,
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("input") JsonNode input,
            @JsonProperty("tool_use_id") String toolUseId,
            @JsonProperty("content") String resultContent) {
        this.type = type;
        this.text = text;
        this.id = id;
        this.name = name;
        this.input = input;
        this.toolUseId = toolUseId;
        this.resultContent = resultContent;
    }

    public static ContentBlock text(String text) {
        return new ContentBlock("text", text, null, null, null, null, null);
    }

    public static ContentBlock toolUse(String id, String name, JsonNode input) {
        return new ContentBlock("tool_use", null, id, name, input, null, null);
    }

    public static ContentBlock toolResult(String toolUseId, String resultContent) {
        return new ContentBlock("tool_result", null, null, null, null, toolUseId, resultContent);
    }

    public boolean isText() {
        return "text".equals(type);
    }

    public boolean isToolUse() {
        return "tool_use".equals(type);
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public JsonNode getInput() {
        return input;
    }

    public String getToolUseId() {
        return toolUseId;
    }

    public String getResultContent() {
        return resultContent;
    }
}
