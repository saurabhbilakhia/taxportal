package com.taxportal.clientportal.service

import com.taxportal.clientportal.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val secretKey: String,
    @Value("\${jwt.expiration:86400000}")
    private val expirationTime: Long
) {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun generateToken(user: User): String {
        val now = Date()
        val expiry = Date(now.time + expirationTime)

        return Jwts.builder()
            .subject(user.email)
            .claim("userId", user.id.toString())
            .claim("firstName", user.firstName)
            .claim("lastName", user.lastName)
            .claim("role", user.role.name)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun extractEmail(token: String): String? {
        return extractClaim(token) { it.subject }
    }

    fun extractUserId(token: String): String? {
        return extractClaim(token) { it["userId"] as? String }
    }

    fun extractRole(token: String): String? {
        return extractClaim(token) { it["role"] as? String }
    }

    fun isTokenValid(token: String, email: String): Boolean {
        val extractedEmail = extractEmail(token)
        return extractedEmail == email && !isTokenExpired(token)
    }

    fun getExpirationTime(): Long = expirationTime

    private fun isTokenExpired(token: String): Boolean {
        val expiration = extractClaim(token) { it.expiration }
        return expiration?.before(Date()) ?: true
    }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T?): T? {
        return try {
            val claims = extractAllClaims(token)
            claimsResolver(claims)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
