package com.aubin.pia.infrastructure.agent.claude.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public final class ToolDefinition {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("input_schema")
    private final Map<String, Object> inputSchema;

    public ToolDefinition(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema == null ? null : Map.copyOf(inputSchema);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema == null ? null : Map.copyOf(inputSchema);
    }
}
