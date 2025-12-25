package com.taxportal.clientportal.event

import com.taxportal.clientportal.service.ExtractionService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ExtractionEventListener(
    private val extractionService: ExtractionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async("extractionExecutor")
    @EventListener
    fun handlePaymentCompleted(event: PaymentCompletedEvent) {
        logger.info("Received payment completed event for order: ${event.orderId}")
        try {
            extractionService.triggerExtractionForOrder(event.orderId)
        } catch (e: Exception) {
            logger.error("Failed to trigger extraction for order: ${event.orderId}", e)
        }
    }
}
