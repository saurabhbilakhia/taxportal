package com.taxportal.clientportal.repository

import com.taxportal.clientportal.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentRepository : JpaRepository<Payment, UUID> {
    fun findByOrderId(orderId: UUID): List<Payment>

    fun findByStripeSessionId(stripeSessionId: String): Payment?

    fun findByStripePaymentIntentId(stripePaymentIntentId: String): Payment?
}
