# pia-bootstrap

Executable assembly of PIA Core.

## Responsibility

- Declares the `@SpringBootApplication` main class.
- Activates profiles (`local`, `dev`, `prod`).
- Hosts `application.yml` and Logback configuration.
- Repackages an executable JAR consumed by the distroless Docker image.

## Rules

- **No business code here.** This module is pure glue.
- **Never depends on** `pia-domain` directly — access is mediated by the
  application layer.
- Startup fails fast if any required SSM parameter is missing.
