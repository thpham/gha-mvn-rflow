package com.example.myproject.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application entry point.
 */
@SpringBootApplication(scanBasePackages = ["com.example.myproject"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
