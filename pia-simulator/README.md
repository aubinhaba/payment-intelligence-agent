# pia-simulator

Stand-alone Spring Boot application that emits synthetic payment events.

## Responsibility

Produces realistic traffic to feed the PIA Core:

- **Baseline scenario** — 10–100 tx/s, Poisson inter-arrival.
- **Burst scenario** — ramp to 500 tx/s for 1 minute.
- **Fraud scenarios** — velocity bursts, card testing, geo jumps, amount outliers.

Scenarios are driven by a scheduled `EventGeneratorService` and selected via
configuration so they can be triggered on demand.

## Rules

- Deployed as a **distinct ECS service** so load generation does not contend
  with the analysis service.
- Writes to the same SQS queue consumed by `pia-infrastructure`.
- **Never generates real PANs** — only tokenized `CardReference` values.
