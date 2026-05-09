# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy POMs first — this layer is cached as long as no POM changes
COPY pom.xml .
COPY pia-domain/pom.xml        pia-domain/
COPY pia-application/pom.xml   pia-application/
COPY pia-infrastructure/pom.xml pia-infrastructure/
COPY pia-api/pom.xml           pia-api/
COPY pia-simulator/pom.xml     pia-simulator/
COPY pia-bootstrap/pom.xml     pia-bootstrap/

# Resolve dependencies offline (cached layer)
RUN mvn -B -ntp dependency:resolve dependency:resolve-plugins \
        -pl pia-simulator -am -DskipTests || true

# Copy sources and build
COPY pia-domain/src        pia-domain/src
COPY pia-application/src   pia-application/src
COPY pia-infrastructure/src pia-infrastructure/src
COPY pia-simulator/src     pia-simulator/src

RUN mvn -B -ntp package -pl pia-simulator -am -DskipTests

# ── Stage 2: Runtime (distroless — no shell, non-root, minimal attack surface) ─
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

COPY --from=build /app/pia-simulator/target/pia-simulator-*.jar app.jar

# Container-aware heap sizing: 75% of cgroup limit
ENTRYPOINT [ \
  "java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar" \
]
