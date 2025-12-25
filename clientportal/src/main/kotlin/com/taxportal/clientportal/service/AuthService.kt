package com.taxportal.clientportal.service

import com.taxportal.clientportal.dto.*
import com.taxportal.clientportal.entity.AccountStatus
import com.taxportal.clientportal.entity.User
import com.taxportal.clientportal.entity.UserRole
import com.taxportal.clientportal.exception.ConflictException
import com.taxportal.clientportal.exception.ForbiddenException
import com.taxportal.clientportal.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email already registered")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            phone = request.phone
        )

        val savedUser = userRepository.save(user)
        val token = jwtService.generateToken(savedUser)

        return AuthResponse(
            token = token,
            expiresIn = jwtService.getExpirationTime(),
            user = UserResponse(
                id = savedUser.id.toString(),
                email = savedUser.email,
                firstName = savedUser.firstName,
                lastName = savedUser.lastName,
                role = savedUser.role.name,
                status = savedUser.status.name
            )
        )
    }

    @Transactional
    fun registerClient(request: ClientRegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email already registered")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            phone = request.phone,
            role = UserRole.CLIENT,
            status = AccountStatus.APPROVED
        )

        val savedUser = userRepository.save(user)
        val token = jwtService.generateToken(savedUser)

        return AuthResponse(
            token = token,
            expiresIn = jwtService.getExpirationTime(),
            user = UserResponse(
                id = savedUser.id.toString(),
                email = savedUser.email,
                firstName = savedUser.firstName,
                lastName = savedUser.lastName,
                role = savedUser.role.name,
                status = savedUser.status.name
            )
        )
    }

    @Transactional
    fun registerAccountant(request: AccountantRegisterRequest): AccountantRegisterResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email already registered")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            phone = request.phone,
            role = UserRole.ACCOUNTANT,
            status = AccountStatus.PENDING,
            licenseNumber = request.licenseNumber,
            firmName = request.firmName
        )

        userRepository.save(user)

        return AccountantRegisterResponse(
            message = "Registration submitted. Awaiting admin approval.",
            status = "PENDING"
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val user = userRepository.findByEmail(request.email)!!

        if (user.status == AccountStatus.PENDING) {
            throw ForbiddenException("Account pending approval")
        }
        if (user.status == AccountStatus.REJECTED) {
            throw ForbiddenException("Account has been rejected")
        }

        val token = jwtService.generateToken(user)

        return AuthResponse(
            token = token,
            expiresIn = jwtService.getExpirationTime(),
            user = UserResponse(
                id = user.id.toString(),
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role.name,
                status = user.status.name
            )
        )
    }

    fun getCurrentUser(email: String): UserResponse {
        val user = userRepository.findByEmail(email)!!
        return UserResponse(
            id = user.id.toString(),
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            role = user.role.name,
            status = user.status.name
        )
    }
}
