package com.example.myproject.core.service

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.springframework.stereotype.Service

/**
 * Service for health check operations with observability.
 */
@Service
class HealthService(
    private val observationRegistry: ObservationRegistry,
) {
    /**
     * Returns the current health status.
     */
    fun getStatus(): String = "UP"

    /**
     * Returns detailed health information with custom observation/span.
     */
    fun getHealthDetails(): Map<String, Any> =
        Observation
            .createNotStarted("health.details.check", observationRegistry)
            .lowCardinalityKeyValue("operation", "health-check")
            .lowCardinalityKeyValue("component", "core")
            .observe<Map<String, Any>> {
                buildHealthDetails()
            } ?: buildHealthDetails()

    private fun buildHealthDetails(): Map<String, Any> =
        mapOf(
            "status" to getStatus(),
            "components" to
                mapOf(
                    "core" to mapOf("status" to "UP"),
                    "common" to mapOf("status" to "UP"),
                ),
            "observability" to
                mapOf(
                    "tracing" to "enabled",
                    "metrics" to "enabled",
                ),
        )
}
