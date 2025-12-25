package com.taxportal.clientportal.event

import java.util.*

data class PaymentCompletedEvent(
    val orderId: UUID,
    val paymentId: UUID
)
