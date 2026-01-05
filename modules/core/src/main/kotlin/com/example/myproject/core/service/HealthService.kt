package com.example.myproject.core.service

import org.springframework.stereotype.Service

/**
 * Service for health check operations.
 */
@Service
class HealthService {
    /**
     * Returns the current health status.
     */
    fun getStatus(): String = "UP"

    /**
     * Returns detailed health information.
     */
    fun getHealthDetails(): Map<String, Any> =
        mapOf(
            "status" to getStatus(),
            "components" to
                mapOf(
                    "core" to mapOf("status" to "UP"),
                    "common" to mapOf("status" to "UP"),
                ),
        )
}
