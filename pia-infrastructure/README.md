# pia-infrastructure

Outward-facing adapters implementing the ports declared in `pia-application`.

## Responsibility

Translates between domain concepts and concrete technologies:

- **Persistence** — `DynamoDbTransactionRepository`, `DynamoDbAnomalyRepository`, outbox publisher.
- **Storage** — `S3ReportStorage`.
- **Messaging** — SQS listener with retry, DLQ routing and idempotency.
- **Agent** — `ClaudeAgentAdapter`, `ClaudeApiClient` (WebClient), `ToolCallingLoop`, `AgentTool` implementations.
- **Observability** — CloudWatch metrics publisher, structured logging (PAN masking converter).
- **Security** — `SsmSecretsLoader`.

## Rules

- Depends on `pia-application` and AWS / Spring / WebClient libraries.
- **Never imported by** `pia-domain` or `pia-application`.
- Every adapter has a Testcontainers + LocalStack integration test.

## Coverage target

Integration coverage on critical flows; unit coverage **≥ 80 %**.
