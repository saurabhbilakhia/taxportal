package com.taxportal.clientportal.service

import com.taxportal.clientportal.TestUtils
import com.taxportal.clientportal.entity.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwtServiceTest {

    private lateinit var jwtService: JwtService

    companion object {
        private const val TEST_SECRET = "testSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm"
        private const val EXPIRATION_TIME = 86400000L // 24 hours
    }

    @BeforeEach
    fun setUp() {
        jwtService = JwtService(TEST_SECRET, EXPIRATION_TIME)
    }

    @Test
    fun `generateToken - creates valid JWT token`() {
        val user = TestUtils.createTestUser()

        val token = jwtService.generateToken(user)

        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertTrue(token.contains("."))
    }

    @Test
    fun `extractEmail - returns correct email from token`() {
        val user = TestUtils.createTestUser(email = "user@test.com")
        val token = jwtService.generateToken(user)

        val extractedEmail = jwtService.extractEmail(token)

        assertEquals("user@test.com", extractedEmail)
    }

    @Test
    fun `extractUserId - returns correct userId from token`() {
        val user = TestUtils.createTestUser()
        val token = jwtService.generateToken(user)

        val extractedUserId = jwtService.extractUserId(token)

        assertEquals(user.id.toString(), extractedUserId)
    }

    @Test
    fun `extractRole - returns correct role from token`() {
        val user = TestUtils.createTestUser(role = UserRole.ACCOUNTANT)
        val token = jwtService.generateToken(user)

        val extractedRole = jwtService.extractRole(token)

        assertEquals("ACCOUNTANT", extractedRole)
    }

    @Test
    fun `isTokenValid - returns true for valid token`() {
        val user = TestUtils.createTestUser()
        val token = jwtService.generateToken(user)

        val isValid = jwtService.isTokenValid(token, user.email)

        assertTrue(isValid)
    }

    @Test
    fun `isTokenValid - returns false when email does not match`() {
        val user = TestUtils.createTestUser()
        val token = jwtService.generateToken(user)

        val isValid = jwtService.isTokenValid(token, "different@email.com")

        assertFalse(isValid)
    }

    @Test
    fun `isTokenValid - returns false for invalid token`() {
        val isValid = jwtService.isTokenValid("invalid.token.here", TestUtils.TEST_EMAIL)

        assertFalse(isValid)
    }

    @Test
    fun `getExpirationTime - returns configured expiration time`() {
        val expirationTime = jwtService.getExpirationTime()

        assertEquals(EXPIRATION_TIME, expirationTime)
    }

    @Test
    fun `extractEmail - returns null for malformed token`() {
        val extractedEmail = jwtService.extractEmail("not.a.valid.jwt")

        assertEquals(null, extractedEmail)
    }

    @Test
    fun `extractUserId - returns null for malformed token`() {
        val extractedUserId = jwtService.extractUserId("not.a.valid.jwt")

        assertEquals(null, extractedUserId)
    }
}
