package com.taxportal.clientportal.controller

import com.taxportal.clientportal.dto.NanonetsWebhookPayload
import com.taxportal.clientportal.service.ExtractionService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/webhooks")
class NanonetsWebhookController(
    private val extractionService: ExtractionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/nanonets")
    fun handleNanonetsWebhook(
        @RequestBody payload: NanonetsWebhookPayload
    ): ResponseEntity<String> {
        logger.info("Received Nanonets webhook callback")
        logger.debug("Webhook payload: $payload")

        try {
            extractionService.handleWebhookCallback(payload)
            return ResponseEntity.ok("Received")
        } catch (e: Exception) {
            logger.error("Error processing Nanonets webhook", e)
            return ResponseEntity.ok("Received") // Still return 200 to prevent retries
        }
    }
}
