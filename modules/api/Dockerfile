# Multi-stage Dockerfile with optimized layer caching
# Separates external dependencies from internal modules for maximum cache reuse
#
# Uses UBI 10 Minimal base for multi-architecture support (amd64 + arm64)
# Alpine images don't have ARM64 variants for eclipse-temurin
# UBI provides enterprise-grade security and RHEL compatibility
#
# Layer architecture (ordered by change frequency, least to most):
# 1. dependencies        - External JARs (Spring, Jackson) - changes on dep updates
# 2. spring-boot-loader  - Spring Boot loader classes - rarely changes
# 3. snapshot-dependencies - SNAPSHOT dependencies - occasional
# 4. internal-dependencies - Internal modules (common, core) - changes on feature commits
# 5. application         - API module code + resources - changes on every build
#
# IMPORTANT: ARG declarations are placed AFTER COPY commands to preserve layer caching.
# Docker uses ARG values as cache keys - placing them before COPY would invalidate
# the dependencies layer on every build (even when dependencies don't change).

FROM eclipse-temurin:17-jre-ubi10-minimal AS builder

WORKDIR /app

# Copy the pre-built JAR from CI
COPY target/*.jar app.jar

# Extract layers using Spring Boot tools (Spring Boot 4.0+ syntax)
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-ubi10-minimal AS runtime

WORKDIR /app

# Add non-root user for security BEFORE any ARGs that change per-build
# This layer will be cached and reused across builds
# UBI minimal doesn't include shadow-utils by default, but useradd/groupadd are available
RUN groupadd -r appgroup && useradd -r -g appgroup -s /sbin/nologin appuser

# Copy layers in order of change frequency (least → most)
# This maximizes layer cache reuse across builds
# These COPY commands come BEFORE ARG declarations to ensure caching works
#
# Layer 1: External dependencies (Spring, Jackson, etc.) - changes on dep updates
COPY --from=builder --chown=appuser:appgroup /app/extracted/dependencies/ ./
# Layer 2: Spring Boot loader - rarely changes
COPY --from=builder --chown=appuser:appgroup /app/extracted/spring-boot-loader/ ./
# Layer 3: SNAPSHOT dependencies - occasional
COPY --from=builder --chown=appuser:appgroup /app/extracted/snapshot-dependencies/ ./
# Layer 4: Internal modules (common, core) - changes on feature commits
COPY --from=builder --chown=appuser:appgroup /app/extracted/internal-dependencies/ ./
# Layer 5: Application code (api module) - changes on every build
COPY --from=builder --chown=appuser:appgroup /app/extracted/application/ ./

# ─────────────────────────────────────────────────────────────────────────────
# Build-time arguments for OCI labels - declared AFTER COPY commands
# to prevent cache invalidation of the dependency layers above.
# These ARGs change on every build (GIT_SHA, BUILD_TIMESTAMP, VERSION),
# so any instruction AFTER them cannot be cached across builds.
ARG GIT_SHA="unknown"
ARG BUILD_TIMESTAMP="unknown"
ARG VERSION="unknown"
ARG IMAGE_TYPE="unknown"
ARG PR_NUMBER=""
ARG BRANCH_NAME=""
ARG RETENTION="forever"

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

USER appuser

# Expose application port
EXPOSE 8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Use JarLauncher for layered JAR execution
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
