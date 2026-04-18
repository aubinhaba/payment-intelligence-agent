# pia-domain

Pure domain model of the Payment Intelligence Agent.

## Responsibility

Expresses the business language of payment analysis: what a `Transaction`
is, when an `Anomaly` is raised, how a `Report` is structured, and which
`DetectionPolicy` rules apply. It is the heart of the hexagon.

## Rules

- **Zero framework dependency.** No Spring, no Jackson, no AWS, no Lombok.
- **Zero I/O.** The domain never reads from nor writes to anything.
- **Immutability by default.** Aggregates mutate through explicit methods.
- **Pure Java.** Records, sealed interfaces, pattern matching are welcome.
- **Enforced by ArchUnit**.

## Contents (planned)

```
com.aubin.pia.domain
├── transaction     # Transaction aggregate, Amount, CardReference, Merchant
├── anomaly         # Anomaly aggregate, AnomalyType, Severity, DetectionPolicy
├── report          # Report aggregate, ReportContent
└── shared          # AggregateRoot, DomainEvent, Identifier primitives
```

## Coverage target

**95 %+** line coverage, enforced via JaCoCo.
