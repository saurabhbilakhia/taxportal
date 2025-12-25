package com.taxportal.clientportal.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null
)

data class ClientRegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null
)

data class AccountantRegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,

    @field:NotBlank(message = "License number is required")
    val licenseNumber: String,

    @field:NotBlank(message = "Firm name is required")
    val firmName: String
)

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class AuthResponse(
    val token: String,
    val expiresIn: Long,
    val user: UserResponse
)

data class AccountantRegisterResponse(
    val message: String,
    val status: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val role: String? = null,
    val status: String? = null
)
