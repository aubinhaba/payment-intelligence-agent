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
graph TB
    Analyst[👤 Risk Analyst<br/>Reviews reports and alerts]
    PlatformEng[👤 Platform Engineer<br/>Monitors technical health]

    PIA[🎯 Payment Intelligence Agent<br/>Fraud detection<br/>+ AI reports]

    Source[📡 Payment Source<br/>simulated by pia-simulator]
    Claude[🤖 Anthropic Claude API<br/>Contextual analysis]
    SNS[📧 SNS / Email<br/>Operator alerts]

    Source -->|payment events| PIA
    PIA -->|autonomous tool calling| Claude
    PIA -->|CRITICAL alarms| SNS
    PIA -->|dashboard + REST API| Analyst
    PIA -->|logs, metrics, alarms| PlatformEng

    style PIA fill:#1168bd,stroke:#0b4884,color:#fff
    style Analyst fill:#08427b,stroke:#073b6f,color:#fff
    style PlatformEng fill:#08427b,stroke:#073b6f,color:#fff
    style Source fill:#999,stroke:#666,color:#fff
    style Claude fill:#999,stroke:#666,color:#fff
    style SNS fill:#999,stroke:#666,color:#fff
```

### C4 — Container

```mermaid
graph TB
    Source[📡 Event Source<br/>SQS sender]

    subgraph "AWS account — eu-west-1"
        subgraph "VPC pia-dev"
            ALB[⚖️ ALB<br/>Application Load Balancer]

            subgraph "ECS Fargate cluster"
                Core[🟦 pia-core<br/>Spring Boot · Java 21<br/>port 8080]
                Sim[🟦 pia-simulator<br/>Spring Boot scheduler]
            end
        end

        SQS[(📬 SQS<br/>payment-events<br/>+ DLQ)]
        DDB[(💾 DynamoDB<br/>pia-table<br/>single-table design)]
        S3[(🗂️ S3<br/>reports bucket)]
        SSM[(🔐 SSM Parameter Store<br/>SecureString)]

        CF[🌍 CloudFront<br/>Distribution]
        Dashboard[(🗂️ S3<br/>Angular static)]
    end

    Claude[🤖 Claude API<br/>api.anthropic.com]
    Analyst[👤 Risk Analyst]

    Source -->|HTTPS / SigV4| SQS
    Sim -->|generates events| SQS
    SQS -->|consume| Core
    Core -->|R/W| DDB
    Core -->|PUT reports| S3
    Core -->|GET API key| SSM
    Core -->|tool calling loop| Claude
    Core -->|REST API JSON| ALB

    Analyst -->|HTTPS| CF
    CF -->|OAC| Dashboard
    Dashboard -->|fetch| ALB
    ALB -->|HTTP:80| Core

    style Core fill:#1168bd,stroke:#0b4884,color:#fff
    style Sim fill:#1168bd,stroke:#0b4884,color:#fff
    style Source fill:#999,stroke:#666,color:#fff
    style Claude fill:#999,stroke:#666,color:#fff
    style Analyst fill:#08427b,stroke:#073b6f,color:#fff
```

### AI Agent Flow

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
## Hexagonal architecture — Dependency modules

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
