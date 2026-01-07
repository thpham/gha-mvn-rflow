# Observability Stack

This document describes the observability stack implemented for the Spring Boot application, providing comprehensive monitoring through the **four pillars of observability**: Metrics, Traces, Logs, and Profiles.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Spring Boot Application                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  Micrometer │  │   Logback   │  │  Micrometer │  │    Pyroscope SDK    │ │
│  │   Metrics   │  │   + OTLP    │  │   Tracing   │  │  (JFR Profiling)    │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘ │
└─────────┼────────────────┼────────────────┼─────────────────────┼───────────┘
          │                │                │                     │
          │ Prometheus     │ OTLP           │ OTLP                │ Push
          │ (Pull)         │ (Push)         │ (Push)              │
          ▼                ▼                ▼                     ▼
    ┌──────────┐     ┌──────────┐     ┌──────────┐          ┌──────────┐
    │  Alloy   │     │   Loki   │     │  Tempo   │          │Pyroscope │
    │ (Scrape) │     │  (Logs)  │     │ (Traces) │          │(Profiles)│
    └────┬─────┘     └────┬─────┘     └────┬─────┘          └────┬─────┘
         │                │                │                     │
         │ Remote Write   │                │ Span Metrics        │
         ▼                │                ▼                     │
    ┌──────────┐          │           ┌──────────┐               │
    │  Mimir   │◄─────────┼───────────│  Mimir   │               │
    │(Metrics) │          │           │(Metrics) │               │
    └────┬─────┘          │           └────┬─────┘               │
         │                │                │                     │
         └────────────────┴────────────────┴─────────────────────┘
                                   │
                                   ▼
                            ┌───────────┐
                            │  Grafana  │
                            │(Visualize)│
                            └───────────┘
```

## Components

### Grafana Stack (LGTM + Pyroscope)

| Component     | Purpose                                 | Port             | Data Flow                          |
| ------------- | --------------------------------------- | ---------------- | ---------------------------------- |
| **Grafana**   | Visualization & Dashboards              | 3000             | Query all backends                 |
| **Loki**      | Log Aggregation                         | 3100             | OTLP push from app                 |
| **Tempo**     | Distributed Tracing                     | 3200, 4317, 4318 | OTLP push from app                 |
| **Mimir**     | Metrics Storage (Prometheus-compatible) | 9090             | Pull via Alloy, Tempo span metrics |
| **Alloy**     | OpenTelemetry Collector                 | 12345            | Scrapes metrics, forwards to Mimir |
| **Pyroscope** | Continuous Profiling                    | 4040             | Push from Pyroscope SDK            |

### Data Flow Modes

- **Metrics**: Pull mode - Alloy scrapes `/actuator/prometheus` and forwards to Mimir
- **Traces**: Push mode - Application sends OTLP traces to Tempo
- **Logs**: Push mode - Logback OTLP appender sends to Loki
- **Profiles**: Push mode - Pyroscope SDK pushes JFR profiles to Pyroscope server

## Quick Start

### 1. Start the Observability Stack

```bash
docker-compose -f docker-compose.observability.yml up -d
```

### 2. Access the UIs

| Service   | URL                    | Credentials   |
| --------- | ---------------------- | ------------- |
| Grafana   | http://localhost:3000  | admin / admin |
| Mimir     | http://localhost:9090  | -             |
| Tempo     | http://localhost:3200  | -             |
| Loki      | http://localhost:3100  | -             |
| Pyroscope | http://localhost:4040  | -             |
| Alloy     | http://localhost:12345 | -             |

### 3. Run the Application with Observability

```bash
# Full observability (traces, logs, metrics, profiles)
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318/v1/traces \
OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=http://localhost:3100/otlp/v1/logs \
OTEL_METRICS_EXPORT_ENABLED=true \
PYROSCOPE_ENABLED=true \
PYROSCOPE_SERVER_ADDRESS=http://localhost:4040 \
mvn spring-boot:run -pl modules/api
```

### 4. Generate Traffic

```bash
# Health check endpoint
curl http://localhost:8080/api/v1/health

# Generate load for meaningful metrics
for i in {1..100}; do curl -s http://localhost:8080/api/v1/health > /dev/null; done
```

### 5. Explore in Grafana

Navigate to http://localhost:3000 and explore the pre-configured dashboards in the "Observability" folder.

## Configuration

### Environment Variables

| Variable                           | Default                              | Description                         |
| ---------------------------------- | ------------------------------------ | ----------------------------------- |
| `OTEL_EXPORTER_OTLP_ENDPOINT`      | `http://localhost:4318/v1/traces`    | Tempo OTLP HTTP endpoint            |
| `OTEL_EXPORTER_OTLP_LOGS_ENDPOINT` | `http://localhost:3100/otlp/v1/logs` | Loki OTLP endpoint                  |
| `OTEL_METRICS_EXPORT_ENABLED`      | `false`                              | Enable OTLP metrics export          |
| `PYROSCOPE_ENABLED`                | `false`                              | Enable continuous profiling         |
| `PYROSCOPE_SERVER_ADDRESS`         | `http://localhost:4040`              | Pyroscope server URL                |
| `DEPLOYMENT_ENV`                   | `local`                              | Environment label for all telemetry |
| `TRACE_LOG_LEVEL`                  | `INFO`                               | Log level for tracing libraries     |
| `OTEL_LOG_LEVEL`                   | `INFO`                               | Log level for OpenTelemetry         |

### Application Configuration

Key configuration in `application.yml`:

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0 # 100% sampling (use 0.1 in production)
  otlp:
    tracing:
      endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4318/v1/traces}

pyroscope:
  agent:
    enabled: ${PYROSCOPE_ENABLED:false}
    application-name: ${spring.application.name}
    server-address: ${PYROSCOPE_SERVER_ADDRESS:http://localhost:4040}
```

## Grafana Dashboards

Five pre-configured dashboards are auto-provisioned:

### 1. Application Overview

**Purpose**: Service health monitoring using RED metrics (Rate, Errors, Duration)

**Key Panels**:

- Success Rate, Request Rate, P95 Latency, Errors/min
- Request rate by pod (for horizontal scaling visibility)
- Response time percentiles (P50, P90, P95, P99)
- Status code distribution (2xx, 4xx, 5xx)
- Top endpoints by request rate
- Endpoint performance table

**Variables**: Namespace, Application, Pod

### 2. JVM Performance

**Purpose**: JVM resource monitoring for Java/Kotlin applications

**Key Panels**:

- Heap usage gauge with thresholds
- Heap/Non-heap memory by pod
- Memory pools (Eden, Survivor, Old Gen, Metaspace, etc.)
- GC pauses by action and cause
- GC pause time and allocation rate
- Thread count and thread states
- Process CPU usage
- Open file descriptors

**Variables**: Namespace, Application, Pod

### 3. Traces Explorer

**Purpose**: Distributed tracing analysis using Tempo

**Key Panels**:

- Trace count, error rate, P95 latency (from span metrics)
- Request rate by service (derived from traces)
- Latency percentiles by service
- Errors by service and operation
- Service map (node graph)
- Trace search with filters

**Variables**: Service, Min Duration, Status

### 4. Logs Explorer

**Purpose**: Log aggregation and analysis using Loki

**Key Panels**:

- Log volume statistics (total, errors, warnings)
- Log volume by level (color-coded)
- Log volume by service and pod
- Error analysis by service and pod
- Live log stream with trace ID correlation
- Search panel with text filter
- Trace correlation panel

**Variables**: Namespace, Service, Pod, Level, Search, Trace ID

### 5. Continuous Profiling

**Purpose**: Performance profiling using Pyroscope

**Key Panels**:

- CPU flame graph
- Memory allocation flame graph
- Lock contention flame graph
- Comparison mode (diff) for regression analysis
- Flame graph reading guide

**Variables**: Service, Environment

## Correlation Between Signals

The stack is configured for full correlation between all four pillars:

### Traces → Logs

- Tempo is configured with `tracesToLogsV2` to link to Loki
- Log entries include `traceId` field for correlation
- Click a trace span to see related logs

### Traces → Metrics

- Tempo generates span metrics sent to Mimir
- `tracesToMetrics` links traces to RED metrics
- Service map uses Mimir for node statistics

### Traces → Profiles

- `tracesToProfiles` links traces to Pyroscope
- Click a span to see the CPU profile for that time range
- Useful for identifying why specific requests are slow

### Logs → Traces

- Loki `derivedFields` extracts `traceId` from log messages
- Click a trace ID in logs to open the trace in Tempo

### Metrics → Traces (Exemplars)

- Mimir stores exemplars with trace IDs
- Click a data point to see the trace that generated it

## Production Considerations

### Sampling

For production, reduce trace sampling to manage costs:

```yaml
management:
  tracing:
    sampling:
      probability: 0.1 # 10% sampling
```

### Resource Limits

Add resource limits in Kubernetes deployments:

```yaml
resources:
  limits:
    memory: "512Mi"
    cpu: "500m"
  requests:
    memory: "256Mi"
    cpu: "100m"
```

### Profiling Impact

Pyroscope profiling has minimal overhead (~2-5% CPU), but consider:

- Disabling in extremely latency-sensitive paths
- Using environment variables to enable only in staging/canary

### High Availability

For production, deploy Mimir, Loki, and Tempo in clustered mode:

- Use object storage (S3, GCS, Azure Blob) for long-term storage
- Deploy multiple replicas for query and ingestion
- Configure appropriate retention policies

### Security

- Use TLS for all endpoints in production
- Configure authentication (OAuth, LDAP) for Grafana
- Restrict network access to observability endpoints
- Avoid logging sensitive data (PII, credentials)

## Troubleshooting

### No Metrics in Grafana

1. Check Alloy is scraping: http://localhost:12345
2. Verify application exposes `/actuator/prometheus`
3. Check Mimir is receiving: `curl http://localhost:9090/prometheus/api/v1/labels`

### No Traces in Tempo

1. Verify OTLP endpoint is reachable: `curl http://localhost:4318/v1/traces`
2. Check application logs for OTLP export errors
3. Verify sampling is not set to 0

### No Logs in Loki

1. Check Loki health: `curl http://localhost:3100/ready`
2. Verify OTLP logs endpoint: `curl http://localhost:3100/otlp/v1/logs`
3. Check `logback-spring.xml` for OTLP appender configuration

### No Profiles in Pyroscope

1. Verify `PYROSCOPE_ENABLED=true`
2. Check application logs for "Pyroscope agent started"
3. Verify Pyroscope is reachable: `curl http://localhost:4040/ready`

### Trace IDs Not Appearing in Logs

1. Ensure MDC is configured in logback pattern
2. Check that tracing bridge is active
3. Verify log format includes `%X{traceId}`

## File Structure

```
observability/
├── alloy.config              # Alloy configuration (scrape & forward)
├── grafana-datasources.yml   # Datasource auto-provisioning
├── grafana-dashboards.yml    # Dashboard provisioning config
├── grafana-dashboards/       # Dashboard JSON files
│   ├── application-overview.json
│   ├── jvm-performance.json
│   ├── traces-explorer.json
│   ├── logs-explorer.json
│   └── continuous-profiling.json
├── loki-config.yml           # Loki configuration
├── mimir.yml                 # Mimir configuration
└── tempo.yml                 # Tempo configuration

modules/api/src/main/
├── kotlin/.../config/
│   └── PyroscopeConfig.kt    # Pyroscope SDK initialization
└── resources/
    ├── application.yml       # Observability configuration
    └── logback-spring.xml    # Logging with OTLP export
```

## References

- [Grafana Documentation](https://grafana.com/docs/)
- [Mimir Documentation](https://grafana.com/docs/mimir/latest/)
- [Tempo Documentation](https://grafana.com/docs/tempo/latest/)
- [Loki Documentation](https://grafana.com/docs/loki/latest/)
- [Pyroscope Documentation](https://grafana.com/docs/pyroscope/latest/)
- [Spring Boot Observability](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.observability)
- [Micrometer Tracing](https://micrometer.io/docs/tracing)
- [OpenTelemetry](https://opentelemetry.io/docs/)
