package com.taxportal.clientportal.controller

import com.taxportal.clientportal.dto.*
import com.taxportal.clientportal.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/register/client")
    fun registerClient(@Valid @RequestBody request: ClientRegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.registerClient(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/register/accountant")
    fun registerAccountant(@Valid @RequestBody request: AccountantRegisterRequest): ResponseEntity<AccountantRegisterResponse> {
        val response = authService.registerAccountant(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<UserResponse> {
        val response = authService.getCurrentUser(userDetails.username)
        return ResponseEntity.ok(response)
    }
}
