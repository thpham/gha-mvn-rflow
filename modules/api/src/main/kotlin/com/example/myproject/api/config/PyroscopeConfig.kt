package com.example.myproject.api.config

import io.pyroscope.http.Format
import io.pyroscope.javaagent.EventType
import io.pyroscope.javaagent.PyroscopeAgent
import io.pyroscope.javaagent.config.Config
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
    @Value("\${pyroscope.agent.application-name}") private val applicationName: String,
    @Value("\${pyroscope.agent.server-address}") private val serverAddress: String,
    @Value("\${DEPLOYMENT_ENV:local}") private val environment: String,
) {
    private val logger = LoggerFactory.getLogger(PyroscopeConfig::class.java)

    @PostConstruct
    fun startPyroscope() {
        logger.info("Starting Pyroscope agent for application: $applicationName")

        PyroscopeAgent.start(
            Config
                .Builder()
                .setApplicationName(applicationName)
                .setServerAddress(serverAddress)
                .setFormat(Format.JFR)
                .setProfilingEvent(EventType.ITIMER)
                .setProfilingAlloc("512k")
                .setProfilingLock("10ms")
                .setLabels(
                    mapOf(
                        "environment" to environment,
                        "service" to applicationName,
                    ),
                ).build(),
        )

        logger.info("Pyroscope agent started successfully, pushing profiles to: $serverAddress")
    }
}
