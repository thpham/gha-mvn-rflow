package com.example.myproject.api.config

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenTelemetry configuration for log export via OTLP.
 *
 * This configuration installs the OpenTelemetry Logback appender with the
 * Spring-managed OpenTelemetry instance (auto-configured by Spring Boot 4.0+).
 *
 * Required application properties:
 * - management.opentelemetry.logging.export.otlp.endpoint (e.g., http://localhost:3100/otlp/v1/logs)
 *
 * @see <a href="https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot">OpenTelemetry with Spring Boot</a>
 */
@Configuration
@ConditionalOnClass(OpenTelemetryAppender::class)
class OpenTelemetryConfig {
    /**
     * Installs the OpenTelemetry Logback appender with the Spring-managed OpenTelemetry instance.
     * This enables log export via OTLP to backends like Loki, Grafana Cloud, etc.
     */
    @Bean
    @ConditionalOnProperty(
        name = ["management.opentelemetry.logging.export.otlp.endpoint"],
        matchIfMissing = false,
    )
    fun installOpenTelemetryAppender(openTelemetry: OpenTelemetry): InstallOpenTelemetryAppender =
        InstallOpenTelemetryAppender(openTelemetry)
}

/**
 * Bean that installs the OpenTelemetry Logback appender after initialization.
 * This follows the pattern from Spring Boot's official OpenTelemetry documentation.
 */
class InstallOpenTelemetryAppender(
    private val openTelemetry: OpenTelemetry,
) : InitializingBean {
    private val logger = LoggerFactory.getLogger(InstallOpenTelemetryAppender::class.java)

    override fun afterPropertiesSet() {
        logger.info("Installing OpenTelemetry Logback appender for OTLP log export...")
        OpenTelemetryAppender.install(openTelemetry)
        logger.info("OpenTelemetry Logback appender installed successfully")
    }
}
