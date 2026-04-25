package com.aubin.pia.infrastructure.agent.claude;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aubin.pia.application.port.out.AgentPort;
import com.aubin.pia.domain.anomaly.Anomaly;
import com.aubin.pia.domain.report.ReportContent;
import com.aubin.pia.domain.transaction.Transaction;
import com.aubin.pia.infrastructure.agent.claude.dto.MessageDto;
import com.aubin.pia.infrastructure.agent.claude.dto.ToolDefinition;
import com.aubin.pia.infrastructure.agent.claude.tools.AgentTool;

public class ClaudeAgentAdapter implements AgentPort {

    private final ToolCallingLoop loop;
    private final Map<String, AgentTool> tools;
    private final List<ToolDefinition> toolDefinitions;
    private final String model;
    private final int maxTokens;
    private final String systemPrompt;

    public ClaudeAgentAdapter(
            ToolCallingLoop loop,
            Map<String, AgentTool> tools,
            String model,
            int maxTokens,
            String systemPrompt) {
        this.loop = loop;
        this.tools = tools;
        this.toolDefinitions =
                tools.values().stream()
                        .map(t -> new ToolDefinition(t.name(), t.description(), t.inputSchema()))
                        .collect(Collectors.toList());
        this.model = model;
        this.maxTokens = maxTokens;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public ReportContent analyze(Transaction transaction, List<Anomaly> anomalies) {
        String userMessage = buildUserMessage(transaction, anomalies);
        List<MessageDto> messages = List.of(new MessageDto("user", userMessage));
        return loop.run(model, maxTokens, systemPrompt, messages, toolDefinitions);
    }

    private String buildUserMessage(Transaction transaction, List<Anomaly> anomalies) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze the following transaction for fraud risk:\n\n");
        sb.append("Transaction ID: ").append(transaction.id().value()).append("\n");
        sb.append("Amount: ")
                .append(transaction.amount().value().toPlainString())
                .append(" ")
                .append(transaction.amount().currency().getCurrencyCode())
                .append("\n");
        sb.append("Merchant ID: ").append(transaction.merchant().id()).append("\n");
        sb.append("Merchant MCC: ").append(transaction.merchant().mcc()).append("\n");
        sb.append("Merchant Country: ").append(transaction.merchant().country()).append("\n");
        sb.append("Card (last 4): ").append(transaction.cardReference().last4()).append("\n");
        sb.append("Card Reference Hash: ").append(transaction.cardReference().hash()).append("\n");
        sb.append("Occurred At: ").append(transaction.occurredAt()).append("\n");
        sb.append("Status: ").append(transaction.status().name()).append("\n");

        if (!anomalies.isEmpty()) {
            sb.append("\nDetected anomalies (").append(anomalies.size()).append("):\n");
            for (Anomaly anomaly : anomalies) {
                sb.append("- [")
                        .append(anomaly.severity().name())
                        .append("] ")
                        .append(anomaly.type().name())
                        .append(": ")
                        .append(anomaly.description())
                        .append("\n");
            }
        } else {
            sb.append("\nNo anomalies detected by rule-based policies.\n");
        }

        sb.append(
                "\nUse the available tools to gather additional context and compute a risk score."
                        + " Return a JSON object with fields \"summary\" (one sentence) and"
                        + " \"analysis\" (detailed markdown).");

        return sb.toString();
    }
}
