package com.aubin.pia.infrastructure.agent.claude;

public class AgentIterationLimitException extends RuntimeException {

    public AgentIterationLimitException(int maxIterations) {
        super("Agent exceeded maximum iterations: " + maxIterations);
    }
}
