package com.taxportal.clientportal.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.*

enum class ExtractionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

@Entity
@Table(name = "extraction_results")
class ExtractionResult(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    val document: Document,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column(name = "nanonets_request_id")
    var nanonetsRequestId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ExtractionStatus = ExtractionStatus.PENDING,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_data", columnDefinition = "jsonb")
    var extractedData: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_response", columnDefinition = "jsonb")
    var rawResponse: Map<String, Any>? = null,

    @Column(name = "error_message")
    var errorMessage: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null,

    @Column(name = "completed_at")
    var completedAt: Instant? = null
)
