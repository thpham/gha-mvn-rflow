package com.example.myproject.core.service

import io.micrometer.observation.ObservationRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HealthServiceTest {
    private lateinit var healthService: HealthService

    @BeforeEach
    fun setUp() {
        // Use NOOP registry for unit tests (no tracing overhead)
        healthService = HealthService(ObservationRegistry.NOOP)
    }

    @Test
    fun `getStatus should return UP`() {
        val status = healthService.getStatus()
        assertEquals("UP", status)
    }

    @Test
    fun `getHealthDetails should return status and components`() {
        val details = healthService.getHealthDetails()

        assertEquals("UP", details["status"])
        assertTrue(details.containsKey("components"))

        @Suppress("UNCHECKED_CAST")
        val components = details["components"] as Map<String, Map<String, String>>
        assertEquals("UP", components["core"]?.get("status"))
        assertEquals("UP", components["common"]?.get("status"))
    }

    @Test
    fun `getHealthDetails should include observability status`() {
        val details = healthService.getHealthDetails()

        assertTrue(details.containsKey("observability"))

        @Suppress("UNCHECKED_CAST")
        val observability = details["observability"] as Map<String, String>
        assertEquals("enabled", observability["tracing"])
        assertEquals("enabled", observability["metrics"])
    }
}
