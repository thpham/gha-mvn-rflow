# Multi-stage Dockerfile for MyProject API
# Build stage is handled by CI, this just packages the JAR

FROM eclipse-temurin:17-jre-alpine AS runtime

# Build-time arguments for OCI labels
ARG GIT_SHA="unknown"
ARG BUILD_TIMESTAMP="unknown"
ARG VERSION="unknown"
ARG IMAGE_TYPE="unknown"
ARG PR_NUMBER=""
ARG BRANCH_NAME=""
ARG RETENTION="forever"

WORKDIR /app

# OCI standard labels
LABEL org.opencontainers.image.source="https://github.com/thpham/gha-mvn-rflow"
LABEL org.opencontainers.image.revision="${GIT_SHA}"
LABEL org.opencontainers.image.created="${BUILD_TIMESTAMP}"
LABEL org.opencontainers.image.version="${VERSION}"
LABEL org.opencontainers.image.title="myproject-api"
LABEL org.opencontainers.image.description="MyProject API Service"
LABEL org.opencontainers.image.vendor="Example Organization"
LABEL org.opencontainers.image.licenses="Apache-2.0"

# Custom labels for cleanup automation
LABEL com.example.image.type="${IMAGE_TYPE}"
LABEL com.example.image.pr-number="${PR_NUMBER}"
LABEL com.example.image.branch="${BRANCH_NAME}"
LABEL com.example.image.retention="${RETENTION}"

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the pre-built JAR from CI
COPY --chown=appuser:appgroup target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
