# pia-application

Application layer: use cases + ports.

## Responsibility

Orchestrates the domain to satisfy product intents:

- `IngestTransactionUseCase` — validate and persist an incoming payment event.
- `DetectAnomaliesUseCase` — evaluate detection policies over a transaction.
- `AnalyzeWithAgentUseCase` — delegate deep analysis to the Claude agent.
- `GenerateReportUseCase` — produce, persist and publish a report.

## Rules

- Depends only on `pia-domain` and `slf4j-api`.
- **Zero concrete infrastructure** — only ports (interfaces).
- Ports live in `port/in` (driving) and `port/out` (driven).
- No Spring annotations (wiring happens in `pia-bootstrap`).

## Contents (planned)

```
com.aubin.pia.application
├── usecase
├── port
│   ├── in     # Commands and query objects
│   └── out    # Repositories, storage, agent, event publisher, metrics
└── service    # Domain services when pure-domain scope is insufficient
```

## Coverage target

**90 %+** line coverage, enforced via JaCoCo.
