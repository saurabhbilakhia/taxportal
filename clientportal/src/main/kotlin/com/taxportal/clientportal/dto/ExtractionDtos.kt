package com.taxportal.clientportal.dto

import com.taxportal.clientportal.entity.ExtractionResult
import com.taxportal.clientportal.entity.ExtractionStatus
import java.time.Instant
import java.util.*

data class ExtractionResultResponse(
    val id: UUID,
    val documentId: UUID,
    val documentName: String,
    val status: ExtractionStatus,
    val extractedData: Map<String, Any>?,
    val errorMessage: String?,
    val createdAt: Instant?,
    val completedAt: Instant?
) {
    companion object {
        fun from(result: ExtractionResult): ExtractionResultResponse {
            return ExtractionResultResponse(
                id = result.id!!,
                documentId = result.document.id!!,
                documentName = result.document.originalFileName,
                status = result.status,
                extractedData = result.extractedData,
                errorMessage = result.errorMessage,
                createdAt = result.createdAt,
                completedAt = result.completedAt
            )
        }
    }
}

data class OrderExtractionResponse(
    val orderId: UUID,
    val totalDocuments: Int,
    val completedExtractions: Int,
    val pendingExtractions: Int,
    val failedExtractions: Int,
    val results: List<ExtractionResultResponse>
)

data class TaxSlipData(
    val slipType: String,
    val fields: Map<String, String>
)
