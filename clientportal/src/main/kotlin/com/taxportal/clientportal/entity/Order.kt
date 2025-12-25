package com.taxportal.clientportal.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

enum class OrderStatus {
    OPEN,
    SUBMITTED,
    IN_REVIEW,
    PENDING_APPROVAL,
    FILED,
    CANCELLED
}

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.OPEN,

    @Column(name = "tax_year", nullable = false)
    var taxYear: Int,

    var notes: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null,

    @Column(name = "submitted_at")
    var submittedAt: Instant? = null,

    @Column(name = "filed_at")
    var filedAt: Instant? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val documents: MutableList<Document> = mutableListOf(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val payments: MutableList<Payment> = mutableListOf()
)
