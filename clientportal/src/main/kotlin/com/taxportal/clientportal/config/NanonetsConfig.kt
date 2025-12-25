package com.taxportal.clientportal.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Configuration
class NanonetsConfig(
    @Value("\${nanonets.api-key:}")
    private val apiKey: String,

    @Value("\${nanonets.base-url:https://app.nanonets.com/api/v2}")
    private val baseUrl: String
) {

    @Bean
    fun nanonetsWebClient(): WebClient {
        val credentials = Base64.getEncoder().encodeToString("$apiKey:".toByteArray())

        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic $credentials")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) // 16MB for large responses
            }
            .build()
    }
}
