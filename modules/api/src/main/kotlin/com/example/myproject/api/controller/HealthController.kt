package com.example.myproject.api.controller

import com.example.myproject.common.ApiResponse
import com.example.myproject.common.Constants
import com.example.myproject.core.service.HealthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import kotlin.random.Random

/**
 * REST controller for health check endpoints.
 */
@RestController
@RequestMapping(Constants.API_BASE_PATH)
@Tag(name = "Health", description = "Health check endpoints")
class HealthController(
    private val healthService: HealthService,
) {
    @GetMapping("/health")
    @Operation(summary = "Get health status", description = "Returns the current health status of the application")
    fun health(): ApiResponse<Map<String, Any>> = ApiResponse.success(healthService.getHealthDetails())

    @GetMapping("/info")
    @Operation(summary = "Get application info", description = "Returns application information")
    fun info(): ApiResponse<Map<String, String>> =
        ApiResponse.success(
            mapOf(
                "name" to Constants.APP_NAME,
                "version" to Constants.API_VERSION,
            ),
        )

    @GetMapping("/chaos")
    @Operation(
        summary = "Chaos endpoint for testing",
        description = "Simulates random failures for observability testing. Use errorRate param (0-100) to control failure percentage.",
    )
    fun chaos(
        @RequestParam(defaultValue = "20") errorRate: Int,
        @RequestParam(defaultValue = "0") delayMs: Long,
    ): ApiResponse<Map<String, Any>> {
        // Optional artificial delay for latency testing
        if (delayMs > 0) {
            Thread.sleep(delayMs.coerceAtMost(5000)) // Cap at 5 seconds
        }

        // Random failure based on errorRate percentage
        if (Random.nextInt(100) < errorRate.coerceIn(0, 100)) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Simulated server error for observability testing",
            )
        }

        return ApiResponse.success(
            mapOf(
                "status" to "ok",
                "errorRate" to errorRate,
                "delayMs" to delayMs,
            ),
        )
    }
}
