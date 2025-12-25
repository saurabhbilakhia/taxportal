package com.taxportal.clientportal.dto

import com.taxportal.clientportal.entity.Order
import com.taxportal.clientportal.entity.OrderStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.*

data class CreateOrderRequest(
    @field:NotNull(message = "Tax year is required")
    @field:Min(value = 2000, message = "Tax year must be 2000 or later")
    @field:Max(value = 2100, message = "Tax year must be 2100 or earlier")
    val taxYear: Int,

    val notes: String? = null
)

data class OrderResponse(
    val id: UUID,
    val status: OrderStatus,
    val taxYear: Int,
    val notes: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val submittedAt: Instant?,
    val filedAt: Instant?,
    val documents: List<DocumentResponse>? = null,
    val payments: List<PaymentResponse>? = null
) {
    companion object {
        fun from(order: Order, includeDetails: Boolean = false): OrderResponse {
            return OrderResponse(
                id = order.id!!,
                status = order.status,
                taxYear = order.taxYear,
                notes = order.notes,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                submittedAt = order.submittedAt,
                filedAt = order.filedAt,
                documents = if (includeDetails) order.documents.map { DocumentResponse.from(it) } else null,
                payments = if (includeDetails) order.payments.map { PaymentResponse.from(it) } else null
            )
        }
    }
}

data class OrderListResponse(
    val orders: List<OrderResponse>,
    val total: Int
)
