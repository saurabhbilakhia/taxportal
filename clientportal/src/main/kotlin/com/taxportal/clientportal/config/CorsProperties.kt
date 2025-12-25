package com.taxportal.clientportal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    val allowedOrigins: String = "http://localhost:5173,http://localhost:3000",
    val allowedMethods: String = "GET,POST,PUT,DELETE,OPTIONS,PATCH",
    val allowedHeaders: String = "Authorization,Content-Type,X-Requested-With,Accept,Origin",
    val exposedHeaders: String = "Authorization",
    val allowCredentials: Boolean = true,
    val maxAge: Long = 3600
)
