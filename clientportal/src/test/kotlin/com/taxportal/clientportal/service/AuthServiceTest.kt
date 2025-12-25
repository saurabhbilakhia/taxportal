package com.taxportal.clientportal.service

import com.taxportal.clientportal.TestUtils
import com.taxportal.clientportal.dto.LoginRequest
import com.taxportal.clientportal.dto.RegisterRequest
import com.taxportal.clientportal.entity.User
import com.taxportal.clientportal.exception.ConflictException
import com.taxportal.clientportal.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @MockK
    private lateinit var jwtService: JwtService

    @MockK
    private lateinit var authenticationManager: AuthenticationManager

    @InjectMockKs
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `register - success with valid request`() {
        val request = RegisterRequest(
            email = TestUtils.TEST_EMAIL,
            password = TestUtils.TEST_PASSWORD,
            firstName = TestUtils.TEST_FIRST_NAME,
            lastName = TestUtils.TEST_LAST_NAME,
            phone = TestUtils.TEST_PHONE
        )
        val savedUser = TestUtils.createTestUser()

        every { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns "encodedPassword"
        every { userRepository.save(any<User>()) } returns savedUser
        every { jwtService.generateToken(savedUser) } returns TestUtils.TEST_TOKEN
        every { jwtService.getExpirationTime() } returns 86400000L

        val result = authService.register(request)

        assertNotNull(result)
        assertEquals(TestUtils.TEST_TOKEN, result.token)
        assertEquals(86400000L, result.expiresIn)
        assertEquals(savedUser.email, result.user.email)
        assertEquals(savedUser.firstName, result.user.firstName)
        assertEquals(savedUser.lastName, result.user.lastName)

        verify(exactly = 1) { userRepository.existsByEmail(request.email) }
        verify(exactly = 1) { userRepository.save(any<User>()) }
        verify(exactly = 1) { jwtService.generateToken(savedUser) }
    }

    @Test
    fun `register - throws ConflictException when email already exists`() {
        val request = RegisterRequest(
            email = TestUtils.TEST_EMAIL,
            password = TestUtils.TEST_PASSWORD
        )

        every { userRepository.existsByEmail(request.email) } returns true

        val exception = assertThrows<ConflictException> {
            authService.register(request)
        }

        assertEquals("Email already registered", exception.message)
        verify(exactly = 1) { userRepository.existsByEmail(request.email) }
        verify(exactly = 0) { userRepository.save(any<User>()) }
    }

    @Test
    fun `login - success with valid credentials`() {
        val request = LoginRequest(
            email = TestUtils.TEST_EMAIL,
            password = TestUtils.TEST_PASSWORD
        )
        val user = TestUtils.createTestUser()

        every {
            authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>())
        } returns mockk()
        every { userRepository.findByEmail(request.email) } returns user
        every { jwtService.generateToken(user) } returns TestUtils.TEST_TOKEN
        every { jwtService.getExpirationTime() } returns 86400000L

        val result = authService.login(request)

        assertNotNull(result)
        assertEquals(TestUtils.TEST_TOKEN, result.token)
        assertEquals(user.email, result.user.email)

        verify(exactly = 1) { authenticationManager.authenticate(any()) }
        verify(exactly = 1) { userRepository.findByEmail(request.email) }
        verify(exactly = 1) { jwtService.generateToken(user) }
    }

    @Test
    fun `login - throws exception with invalid credentials`() {
        val request = LoginRequest(
            email = TestUtils.TEST_EMAIL,
            password = "wrongPassword"
        )

        every {
            authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>())
        } throws BadCredentialsException("Invalid credentials")

        assertThrows<BadCredentialsException> {
            authService.login(request)
        }

        verify(exactly = 1) { authenticationManager.authenticate(any()) }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
    }

    @Test
    fun `getCurrentUser - returns user response for valid email`() {
        val user = TestUtils.createTestUser()

        every { userRepository.findByEmail(TestUtils.TEST_EMAIL) } returns user

        val result = authService.getCurrentUser(TestUtils.TEST_EMAIL)

        assertNotNull(result)
        assertEquals(user.id.toString(), result.id)
        assertEquals(user.email, result.email)
        assertEquals(user.firstName, result.firstName)
        assertEquals(user.lastName, result.lastName)

        verify(exactly = 1) { userRepository.findByEmail(TestUtils.TEST_EMAIL) }
    }
}
