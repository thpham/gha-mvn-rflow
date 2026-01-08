package com.example.myproject.api.config

import io.pyroscope.http.Format
import io.pyroscope.javaagent.EventType
import io.pyroscope.javaagent.PyroscopeAgent
import io.pyroscope.javaagent.api.Logger
import io.pyroscope.javaagent.config.Config
import io.pyroscope.javaagent.config.ProfilerType
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

/**
 * Pyroscope continuous profiling configuration.
 * Enables CPU, memory allocation, and lock profiling when enabled.
 */
@Configuration
@ConditionalOnProperty(name = ["pyroscope.agent.enabled"], havingValue = "true")
class PyroscopeConfig(
    @param:Value("\${pyroscope.agent.application-name}") private val applicationName: String,
    @param:Value("\${pyroscope.agent.server-address}") private val serverAddress: String,
    @param:Value("\${DEPLOYMENT_ENV:local}") private val environment: String,
) {
    private val logger = LoggerFactory.getLogger(PyroscopeConfig::class.java)

    @PostConstruct
    fun startPyroscope() {
        logger.info("Starting Pyroscope agent for application: {} at server: {}", applicationName, serverAddress)

        try {
            PyroscopeAgent.start(
                Config
                    .Builder()
                    .setApplicationName(applicationName)
                    .setServerAddress(serverAddress)
                    .setFormat(Format.JFR)
                    // Use JFR profiler (JVM built-in) instead of async-profiler
                    // async-profiler has known issues on macOS ARM64 (Apple Silicon)
                    .setProfilerType(ProfilerType.JFR)
                    .setProfilingEvent(EventType.CPU)
                    .setProfilingAlloc("512k")
                    .setProfilingLock("10ms")
                    .setLogLevel(Logger.Level.DEBUG)
                    // Tenant ID for Grafana Pyroscope multi-tenancy (required for ingest)
                    .setTenantID("anonymous")
                    .setLabels(
                        mapOf(
                            "environment" to environment,
                            "service_name" to applicationName,
                        ),
                    ).build(),
            )
            logger.info("Pyroscope agent started successfully, pushing profiles to: {}", serverAddress)
        } catch (e: Exception) {
            logger.error("Failed to start Pyroscope agent: {}", e.message, e)
        }
    }
}
