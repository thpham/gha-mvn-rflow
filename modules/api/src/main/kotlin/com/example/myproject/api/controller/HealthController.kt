package com.example.myproject.api.controller

import com.example.myproject.common.ApiResponse
import com.example.myproject.common.Constants
import com.example.myproject.core.service.HealthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}
