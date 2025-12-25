package com.taxportal.clientportal.repository

import com.taxportal.clientportal.entity.ExtractionResult
import com.taxportal.clientportal.entity.ExtractionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExtractionResultRepository : JpaRepository<ExtractionResult, UUID> {
    fun findByOrderId(orderId: UUID): List<ExtractionResult>

    fun findByDocumentId(documentId: UUID): ExtractionResult?

    fun findByNanonetsRequestId(nanonetsRequestId: String): ExtractionResult?

    fun findByOrderIdAndStatus(orderId: UUID, status: ExtractionStatus): List<ExtractionResult>

    @Query("SELECT COUNT(e) FROM ExtractionResult e WHERE e.order.id = :orderId AND e.status = :status")
    fun countByOrderIdAndStatus(orderId: UUID, status: ExtractionStatus): Long

    @Query("SELECT COUNT(e) FROM ExtractionResult e WHERE e.order.id = :orderId")
    fun countByOrderId(orderId: UUID): Long
}
