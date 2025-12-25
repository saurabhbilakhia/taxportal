package com.taxportal.clientportal.controller

import com.stripe.model.Event
import com.stripe.net.Webhook
import com.taxportal.clientportal.service.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/webhooks")
class WebhookController(
    private val paymentService: PaymentService,
    @Value("\${stripe.webhook-secret:}")
    private val webhookSecret: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/stripe")
    fun handleStripeWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String
    ): ResponseEntity<String> {
        val event: Event = try {
            if (webhookSecret.isNotBlank() && !webhookSecret.startsWith("whsec_placeholder")) {
                Webhook.constructEvent(payload, signature, webhookSecret)
            } else {
                Event.GSON.fromJson(payload, Event::class.java)
            }
        } catch (e: Exception) {
            logger.error("Webhook signature verification failed", e)
            return ResponseEntity.badRequest().body("Invalid signature")
        }

        when (event.type) {
            "checkout.session.completed" -> {
                val session = event.dataObjectDeserializer.`object`.orElse(null)
                if (session != null) {
                    val sessionId = (session as com.stripe.model.checkout.Session).id
                    logger.info("Checkout session completed: $sessionId")
                    paymentService.handlePaymentSuccess(sessionId)
                }
            }
            "checkout.session.expired" -> {
                val session = event.dataObjectDeserializer.`object`.orElse(null)
                if (session != null) {
                    val sessionId = (session as com.stripe.model.checkout.Session).id
                    logger.info("Checkout session expired: $sessionId")
                    paymentService.handlePaymentFailure(sessionId)
                }
            }
            else -> {
                logger.info("Unhandled event type: ${event.type}")
            }
        }

        return ResponseEntity.ok("Received")
    }
}
