package com.taxportal.clientportal.dto

import java.time.Instant

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val timestamp: Instant = Instant.now()
)

data class ValidationErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val errors: Map<String, String>,
    val timestamp: Instant = Instant.now()
)
