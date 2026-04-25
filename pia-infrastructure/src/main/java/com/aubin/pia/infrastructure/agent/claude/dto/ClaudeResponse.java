package com.aubin.pia.infrastructure.agent.claude.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ClaudeResponse {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("stop_reason")
    private final String stopReason;

    @JsonProperty("content")
    private final List<ContentBlock> content;

    @JsonProperty("usage")
    private final UsageDto usage;

    @JsonCreator
    public ClaudeResponse(
            @JsonProperty("id") String id,
            @JsonProperty("stop_reason") String stopReason,
            @JsonProperty("content") List<ContentBlock> content,
            @JsonProperty("usage") UsageDto usage) {
        this.id = id;
        this.stopReason = stopReason;
        this.content = content;
        this.usage = usage;
    }

    public boolean isEndTurn() {
        return "end_turn".equals(stopReason);
    }

    public boolean isToolUse() {
        return "tool_use".equals(stopReason);
    }

    public List<ContentBlock> toolUseBlocks() {
        return content.stream().filter(ContentBlock::isToolUse).collect(Collectors.toList());
    }

    public String textContent() {
        return content.stream()
                .filter(ContentBlock::isText)
                .map(ContentBlock::getText)
                .collect(Collectors.joining("\n"));
    }

    public String getId() {
        return id;
    }

    public String getStopReason() {
        return stopReason;
    }

    public List<ContentBlock> getContent() {
        return content;
    }

    public UsageDto usage() {
        return usage;
    }
}
