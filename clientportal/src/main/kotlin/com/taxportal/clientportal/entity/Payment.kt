package com.taxportal.clientportal.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column(name = "stripe_session_id")
    var stripeSessionId: String? = null,

    @Column(name = "stripe_payment_intent_id")
    var stripePaymentIntentId: String? = null,

    @Column(name = "amount_cents", nullable = false)
    val amountCents: Int,

    @Column(length = 3)
    val currency: String = "CAD",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,

    @Column(name = "paid_at")
    var paidAt: Instant? = null
)
