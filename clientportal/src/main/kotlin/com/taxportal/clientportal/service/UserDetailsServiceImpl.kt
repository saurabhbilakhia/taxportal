package com.taxportal.clientportal.service

import com.taxportal.clientportal.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        // Map user role to Spring Security authorities
        val authorities = mutableListOf(
            SimpleGrantedAuthority("ROLE_${user.role.name}")
        )

        // Add ROLE_USER as a base role for all authenticated users
        if (user.role.name != "USER") {
            authorities.add(SimpleGrantedAuthority("ROLE_USER"))
        }

        return User.builder()
            .username(user.email)
            .password(user.passwordHash)
            .authorities(authorities)
            .disabled(!user.enabled)
            .build()
    }
}
