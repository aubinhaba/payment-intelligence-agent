# Payment Intelligence Agent (PIA)

> Cloud-native, event-driven payment transaction intelligence agent.
> Ingests simulated payment events, detects anomalies via rules + LLM, and produces automated risk reports through Claude with autonomous tool calling.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg)
![AWS](https://img.shields.io/badge/AWS-ECS%20%7C%20SQS%20%7C%20DynamoDB%20%7C%20S3-ff9900.svg)
![Terraform](https://img.shields.io/badge/Terraform-1.9%2B-623ce4.svg)
![Angular](https://img.shields.io/badge/Angular-18%2B-dd0031.svg)
![Anthropic](https://img.shields.io/badge/Anthropic-Tool%20Calling-black.svg)
![CI](https://github.com/aubinhaba/payment-intelligence-agent/actions/workflows/ci.yml/badge.svg)

---

## Table of contents

- [Why this project exists](#why-this-project-exists)
- [High-level architecture](#high-level-architecture)
- [Repository layout](#repository-layout)
- [Prerequisites](#prerequisites)
- [Quickstart](#quickstart)
- [License](#license)

---

## Why this project exists

PIA is a **portfolio-grade** implementation of a production-ready autonomous analysis agent for payment transactions. It demonstrates:

- **Hexagonal architecture** with strict separation between domain, application and infrastructure.
- **Event-driven ingestion** using SQS with DLQ, idempotent consumers, and the Outbox pattern.
- **Autonomous LLM agent** — the Claude API is wired with a proper tool-calling loop (not a one-shot prompt), giving the model first-class access to domain-aware tools.
- **AWS cloud-native** deployment on ECS Fargate with IaC via Terraform.
- **PCI-DSS mindset**: zero PAN, tokenized card references, PAN masking in logs, SSM secrets, IAM least-privilege.
- **Full observability**: structured JSON logs, Micrometer/CloudWatch metrics, OpenTelemetry traces.

## Architecture

### C4 — Context

```mermaid
C4Context
  title Payment Intelligence Agent — System Context

  Person(analyst, "Risk Analyst", "Reviews anomaly reports and fraud alerts")
  Person(engineer, "Platform Engineer", "Monitors system health and metrics")

  System(pia, "Payment Intelligence Agent", "Ingests payment events, detects anomalies via rules + LLM, generates risk reports")

  System_Ext(claude, "Claude API (Anthropic)", "LLM with tool calling — enriches anomaly analysis")
  System_Ext(simulator, "Event Simulator", "Generates realistic payment events at configurable volume")

  Rel(simulator, pia, "Publishes payment events", "SQS")
  Rel(pia, claude, "Sends analysis prompts + tool results", "HTTPS / Anthropic API")
  Rel(analyst, pia, "Consults reports and alerts", "Angular dashboard / HTTPS")
  Rel(engineer, pia, "Monitors metrics and logs", "CloudWatch dashboard")
```

### C4 — Container

```mermaid
C4Container
  title Payment Intelligence Agent — Containers

  Person(analyst, "Risk Analyst")
  Person(engineer, "Platform Engineer")

  System_Boundary(aws, "AWS — eu-west-1") {
    Container(sim, "pia-simulator", "Spring Boot / Fargate", "Generates payment events on a configurable schedule")
    ContainerDb(sqs, "SQS", "AWS SQS", "payment-events queue + DLQ")
    Container(core, "pia-core", "Spring Boot / Fargate", "Ingestion, anomaly detection, agent orchestration, report generation")
    ContainerDb(dynamo, "DynamoDB", "AWS DynamoDB", "Single-table: transactions, anomalies, reports, outbox")
    ContainerDb(s3, "S3 reports", "AWS S3", "JSON + Markdown analysis reports")
    Container(api, "REST BFF", "Spring MVC (embedded in pia-core)", "API endpoints for the Angular dashboard")
    Container(dashboard, "Angular dashboard", "Angular 18 / S3 + CloudFront", "Risk analyst UI: transactions, anomalies, reports, KPIs")
    Container(cw, "CloudWatch", "AWS CloudWatch", "Metrics, alarms, structured logs, SNS alerts")
    Container(alb, "ALB", "AWS ALB", "Routes HTTP traffic to pia-core API")
  }

  System_Ext(claude, "Claude API")

  Rel(sim, sqs, "Publishes PaymentEvent JSON")
  Rel(sqs, core, "Consumes events", "SQS listener")
  Rel(core, dynamo, "Reads/writes transactions, anomalies, outbox")
  Rel(core, s3, "Stores reports")
  Rel(core, claude, "Tool-calling loop", "HTTPS")
  Rel(core, cw, "Publishes metrics + logs")
  Rel(analyst, alb, "HTTP", "browser")
  Rel(alb, api, "Forwards requests")
  Rel(api, dynamo, "Queries")
  Rel(api, s3, "Fetches reports")
  Rel(analyst, dashboard, "Uses", "CloudFront HTTPS")
  Rel(engineer, cw, "Monitors")
```

### Flux agent IA

```mermaid
sequenceDiagram
    participant SQS
    participant Core as pia-core
    participant DDB as DynamoDB
    participant Claude as Claude API
    participant S3

    SQS->>Core: PaymentEvent
    Core->>DDB: persist Transaction
    Core->>Core: DetectAnomaliesUseCase (rules)
    alt anomaly detected
        Core->>Claude: analyze(context) + tools definition
        loop tool calling (max 10 turns)
            Claude-->>Core: tool_use request
            Core->>DDB: get_transaction_history / fetch_similar_anomalies
            Core->>Claude: tool_result
        end
        Claude-->>Core: stop_reason=end_turn + analysis
        Core->>S3: store report (JSON + Markdown)
        Core->>DDB: persist Report metadata
        Core->>Core: publish CloudWatch alarm if severity=HIGH
    end
```
## Architecture Hexagonale — Dépendances modules

```mermaid
graph TD
    subgraph "pia-domain (pure Java)"
        D[Transaction<br/>Anomaly<br/>Report<br/>Policies]
    end

    subgraph "pia-application (use cases + ports)"
        A[IngestTransactionUseCase<br/>DetectAnomaliesUseCase<br/>AnalyzeWithAgentUseCase<br/>GenerateReportUseCase]
        P[Ports: TransactionRepository<br/>AgentPort / EventPublisher<br/>ReportStorage / MetricsPublisher]
    end

    subgraph "pia-infrastructure (adapters)"
        I1[DynamoDB adapter]
        I2[SQS listener]
        I3[S3 adapter]
        I4[ClaudeAgentAdapter<br/>+ ToolCallingLoop]
        I5[CloudWatch metrics]
    end

    subgraph "pia-api (REST BFF)"
        API[TransactionController<br/>AnomalyController<br/>ReportController<br/>MetricsController]
    end

    subgraph "pia-bootstrap (Spring Boot assembly)"
        B[Application + Config]
    end

    A --> D
    P --> A
    I1 --> P
    I2 --> P
    I3 --> P
    I4 --> P
    I5 --> P
    API --> A
    B --> I1
    B --> I2
    B --> I3
    B --> I4
    B --> I5
    B --> API

    style D fill:#e8f5e9,stroke:#2e7d32
    style A fill:#e3f2fd,stroke:#1565c0
    style P fill:#e3f2fd,stroke:#1565c0
    style I1 fill:#fff3e0,stroke:#e65100
    style I2 fill:#fff3e0,stroke:#e65100
    style I3 fill:#fff3e0,stroke:#e65100
    style I4 fill:#fff3e0,stroke:#e65100
    style I5 fill:#fff3e0,stroke:#e65100
    style API fill:#fce4ec,stroke:#880e4f
    style B fill:#f3e5f5,stroke:#4a148c
```

## Repository layout

```
payment-intelligence-agent/
├── pia-domain/          # Pure Java domain, no framework dependency
├── pia-application/     # Use cases + ports
├── pia-infrastructure/  # Adapters: DynamoDB, SQS, S3, Claude API
├── pia-api/             # REST BFF for the Angular dashboard
├── pia-simulator/       # Deterministic + stochastic event generator
├── pia-bootstrap/       # Main Spring Boot application assembly
├── dashboard/           # Angular app
└── terraform/           # Reusable modules + dev/prod environments
```

## Prerequisites

| Tool          | Version  | Purpose                           |
|---------------|----------|-----------------------------------|
| JDK           | 21       | Build and run the JVM services    |
| Maven         | 3.8+     | Build orchestrator                |
| Docker        | 24+      | LocalStack, Testcontainers, build |
| Docker Compose| v2       | Local dev orchestration           |
| Node.js       | 20 LTS   | Angular dashboard                 |
| Terraform     | 1.9+     | Cloud provisioning                |

Optional but recommended: `awscli`, `jq`.

## Quickstart

> The commands below validate the current state of the skeleton.

```bash
# Compile every module and run unit tests
mvn clean install -DskipITs

# Apply Google Java Format (AOSP)
mvn spotless:apply

# Full verification (unit + quality plugins)
mvn verify
```

## License

Released under the [MIT License](./LICENSE).
