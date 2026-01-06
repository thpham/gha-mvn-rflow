package com.example.myproject.common

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

/**
 * Generic API response wrapper for consistent response format
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: Instant = Instant.now(),
) {
    companion object {
        fun <T> success(
            data: T,
            message: String? = null,
        ): ApiResponse<T> = ApiResponse(success = true, data = data, message = message)

        fun <T> error(message: String): ApiResponse<T> = ApiResponse(success = false, message = message)
    }
}
