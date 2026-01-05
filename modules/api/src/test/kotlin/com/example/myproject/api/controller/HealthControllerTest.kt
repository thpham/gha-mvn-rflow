package com.example.myproject.api.controller

import com.example.myproject.common.Constants
import com.example.myproject.core.service.HealthService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(HealthController::class)
class HealthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var healthService: HealthService

    @Test
    fun `health endpoint should return health details`() {
        every { healthService.getHealthDetails() } returns
            mapOf(
                "status" to "UP",
                "components" to mapOf("core" to mapOf("status" to "UP")),
            )

        mockMvc
            .get("${Constants.API_BASE_PATH}/health") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.status") { value("UP") }
            }
    }

    @Test
    fun `info endpoint should return application info`() {
        mockMvc
            .get("${Constants.API_BASE_PATH}/info") {
                accept = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.name") { value(Constants.APP_NAME) }
                jsonPath("$.data.version") { value(Constants.API_VERSION) }
            }
    }
}
