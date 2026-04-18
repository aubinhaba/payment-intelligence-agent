# Payment Intelligence Agent (PIA)

> Cloud-native, event-driven payment transaction intelligence agent.
> Ingests simulated payment events, detects anomalies via rules + LLM, and produces automated risk reports through Claude with autonomous tool calling.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg)
![AWS](https://img.shields.io/badge/AWS-ECS%20%7C%20SQS%20%7C%20DynamoDB%20%7C%20S3-ff9900.svg)
![Terraform](https://img.shields.io/badge/Terraform-1.9%2B-623ce4.svg)
![Angular](https://img.shields.io/badge/Angular-18%2B-dd0031.svg)

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

## High-level architecture

```mermaid
flowchart LR
    SIM[Event simulator] -->|PaymentEvent| SQS[[SQS queue + DLQ]]
    SQS --> CORE[PIA Core<br/>Spring Boot]
    CORE -->|tx, anomaly| DDB[(DynamoDB)]
    CORE -->|report blob| S3[(S3 reports)]
    CORE -->|tool calls + prompts| CLAUDE[Claude API]
    CORE -->|metrics| CW[CloudWatch]
    CW -->|alarms| SNS[SNS alerts]
    DDB --> API[REST BFF]
    S3 --> API
    API --> UI[Angular dashboard]
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
