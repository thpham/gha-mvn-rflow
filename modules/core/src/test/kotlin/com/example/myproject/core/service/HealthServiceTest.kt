package com.example.myproject.core.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HealthServiceTest {
    private lateinit var healthService: HealthService

    @BeforeEach
    fun setUp() {
        healthService = HealthService()
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
}
