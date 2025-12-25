package com.taxportal.clientportal.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.*

@Entity
@Table(name = "extraction_overrides")
class ExtractionOverride(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extraction_result_id", nullable = false)
    val extractionResult: ExtractionResult,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "previous_data", nullable = false, columnDefinition = "jsonb")
    val previousData: Map<String, Any>,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_data", nullable = false, columnDefinition = "jsonb")
    val newData: Map<String, Any>,

    @Column(name = "override_reason")
    val overrideReason: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overridden_by", nullable = false)
    val overriddenBy: User,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null
)
