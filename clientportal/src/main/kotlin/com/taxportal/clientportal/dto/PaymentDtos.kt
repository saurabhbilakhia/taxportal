package com.taxportal.clientportal.dto

import com.taxportal.clientportal.entity.Payment
import com.taxportal.clientportal.entity.PaymentStatus
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.*

data class CreateCheckoutRequest(
    @field:NotBlank(message = "Success URL is required")
    val successUrl: String,

    @field:NotBlank(message = "Cancel URL is required")
    val cancelUrl: String
)

data class CheckoutResponse(
    val checkoutUrl: String,
    val sessionId: String
)

data class PaymentResponse(
    val id: UUID,
    val amountCents: Int,
    val currency: String,
    val status: PaymentStatus,
    val createdAt: Instant?,
    val paidAt: Instant?
) {
    companion object {
        fun from(payment: Payment): PaymentResponse {
            return PaymentResponse(
                id = payment.id!!,
                amountCents = payment.amountCents,
                currency = payment.currency,
                status = payment.status,
                createdAt = payment.createdAt,
                paidAt = payment.paidAt
            )
        }
    }
}
