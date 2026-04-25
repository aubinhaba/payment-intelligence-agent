package com.aubin.pia.infrastructure.agent.claude.tools;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/** Contract for a Claude API tool: provides schema definition and executes calls from Claude. */
public interface AgentTool {

    String name();

    String description();

    Map<String, Object> inputSchema();

    String execute(JsonNode input);
}
