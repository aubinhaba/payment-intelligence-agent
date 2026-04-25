package com.aubin.pia.config;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.aubin.pia.application.port.out.AgentPort;
import com.aubin.pia.application.port.out.AnomalyRepository;
import com.aubin.pia.application.port.out.TransactionRepository;
import com.aubin.pia.infrastructure.agent.claude.ClaudeAgentAdapter;
import com.aubin.pia.infrastructure.agent.claude.ClaudeApiClient;
import com.aubin.pia.infrastructure.agent.claude.ToolCallingLoop;
import com.aubin.pia.infrastructure.agent.claude.tools.AgentTool;
import com.aubin.pia.infrastructure.agent.claude.tools.AggregateByMerchantTool;
import com.aubin.pia.infrastructure.agent.claude.tools.ComputeRiskScoreTool;
import com.aubin.pia.infrastructure.agent.claude.tools.FetchSimilarAnomaliesTool;
import com.aubin.pia.infrastructure.agent.claude.tools.GetTransactionHistoryTool;

@Configuration
public class ClaudeAgentConfig {

    @Bean
    public WebClient claudeWebClient(@Value("${pia.claude.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public ClaudeApiClient claudeApiClient(
            WebClient claudeWebClient,
            @Value("${pia.claude.api-key}") String apiKey,
            ObjectMapper objectMapper) {
        return new ClaudeApiClient(claudeWebClient, apiKey, objectMapper);
    }

    @Bean
    public GetTransactionHistoryTool getTransactionHistoryTool(
            TransactionRepository transactionRepository, ObjectMapper objectMapper) {
        return new GetTransactionHistoryTool(transactionRepository, objectMapper);
    }

    @Bean
    public AggregateByMerchantTool aggregateByMerchantTool(
            TransactionRepository transactionRepository, ObjectMapper objectMapper) {
        return new AggregateByMerchantTool(transactionRepository, objectMapper);
    }

    @Bean
    public FetchSimilarAnomaliesTool fetchSimilarAnomaliesTool(
            AnomalyRepository anomalyRepository, ObjectMapper objectMapper) {
        return new FetchSimilarAnomaliesTool(anomalyRepository, objectMapper);
    }

    @Bean
    public ComputeRiskScoreTool computeRiskScoreTool(
            TransactionRepository transactionRepository,
            AnomalyRepository anomalyRepository,
            @Value("${pia.detection.geo.high-risk-countries}") String countriesCsv,
            ObjectMapper objectMapper) {
        Set<String> countries =
                Arrays.stream(countriesCsv.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());
        return new ComputeRiskScoreTool(
                transactionRepository, anomalyRepository, countries, objectMapper);
    }

    @Bean
    public Map<String, AgentTool> agentToolRegistry(
            GetTransactionHistoryTool getTransactionHistoryTool,
            AggregateByMerchantTool aggregateByMerchantTool,
            FetchSimilarAnomaliesTool fetchSimilarAnomaliesTool,
            ComputeRiskScoreTool computeRiskScoreTool) {
        return Map.of(
                getTransactionHistoryTool.name(), getTransactionHistoryTool,
                aggregateByMerchantTool.name(), aggregateByMerchantTool,
                fetchSimilarAnomaliesTool.name(), fetchSimilarAnomaliesTool,
                computeRiskScoreTool.name(), computeRiskScoreTool);
    }

    @Bean
    public ToolCallingLoop toolCallingLoop(
            ClaudeApiClient claudeApiClient,
            Map<String, AgentTool> agentToolRegistry,
            @Value("${pia.claude.max-iterations}") int maxIterations,
            ObjectMapper objectMapper) {
        return new ToolCallingLoop(claudeApiClient, agentToolRegistry, maxIterations, objectMapper);
    }

    @Bean
    public AgentPort claudeAgentAdapter(
            ToolCallingLoop toolCallingLoop,
            Map<String, AgentTool> agentToolRegistry,
            @Value("${pia.claude.model}") String model,
            @Value("${pia.claude.max-tokens}") int maxTokens,
            @Value("${pia.claude.system-prompt}") String systemPrompt) {
        return new ClaudeAgentAdapter(
                toolCallingLoop, agentToolRegistry, model, maxTokens, systemPrompt);
    }
}
