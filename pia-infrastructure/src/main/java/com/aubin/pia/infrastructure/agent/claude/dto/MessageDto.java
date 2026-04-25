package com.aubin.pia.infrastructure.agent.claude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single entry in the Claude API messages array. {@code content} is {@code Object} because it can
 * be a plain {@code String} (simple user text) or a {@code List<ContentBlock>} (structured tool-use
 * or tool-result turns).
 */
public final class MessageDto {

    @JsonProperty("role")
    private final String role;

    @JsonProperty("content")
    private final Object content;

    public MessageDto(String role, Object content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public Object getContent() {
        return content;
    }
}
