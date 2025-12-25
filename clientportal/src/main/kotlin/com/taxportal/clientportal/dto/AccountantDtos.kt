package com.taxportal.clientportal.dto

import com.taxportal.clientportal.entity.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDate
import java.util.*

// ==================== Dashboard ====================

data class DashboardStatsResponse(
    val totalOrders: Long,
    val ordersByStatus: Map<String, Long>,
    val pendingReview: Long,
    val filedThisMonth: Long,
    val filedThisYear: Long,
    val totalClients: Long
)

// ==================== Order Search/List ====================

data class OrderSearchRequest(
    val status: OrderStatus? = null,
    val clientEmail: String? = null,
    val taxYear: Int? = null,
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
)

data class OrderListItemResponse(
    val id: UUID,
    val clientEmail: String,
    val clientName: String?,
    val taxYear: Int,
    val status: OrderStatus,
    val documentCount: Int,
    val createdAt: Instant?,
    val submittedAt: Instant?,
    val filedAt: Instant?
) {
    companion object {
        fun from(order: Order): OrderListItemResponse {
            val clientName = listOfNotNull(order.user.firstName, order.user.lastName)
                .joinToString(" ")
                .ifBlank { null }

            return OrderListItemResponse(
                id = order.id!!,
                clientEmail = order.user.email,
                clientName = clientName,
                taxYear = order.taxYear,
                status = order.status,
                documentCount = order.documents.size,
                createdAt = order.createdAt,
                submittedAt = order.submittedAt,
                filedAt = order.filedAt
            )
        }
    }
}

data class OrderPageResponse(
    val orders: List<OrderListItemResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)

// ==================== Order Status Update ====================

data class UpdateOrderStatusRequest(
    @field:NotNull(message = "Status is required")
    val status: OrderStatus
)

data class BulkStatusUpdateRequest(
    @field:NotEmpty(message = "Order IDs are required")
    val orderIds: List<UUID>,

    @field:NotNull(message = "Status is required")
    val status: OrderStatus
)

data class BulkStatusUpdateResponse(
    val successCount: Int,
    val failedCount: Int,
    val failures: List<BulkUpdateFailure>
)

data class BulkUpdateFailure(
    val orderId: UUID,
    val reason: String
)

// ==================== Extraction Override ====================

data class ExtractionOverrideRequest(
    @field:NotNull(message = "Extracted data is required")
    val extractedData: Map<String, Any>,

    val reason: String? = null
)

data class ExtractionOverrideResponse(
    val id: UUID,
    val documentId: UUID,
    val previousData: Map<String, Any>?,
    val newData: Map<String, Any>,
    val reason: String?,
    val overriddenBy: String,
    val createdAt: Instant?
) {
    companion object {
        fun from(override: ExtractionOverride): ExtractionOverrideResponse {
            return ExtractionOverrideResponse(
                id = override.id!!,
                documentId = override.extractionResult.document.id!!,
                previousData = override.previousData,
                newData = override.newData,
                reason = override.overrideReason,
                overriddenBy = override.overriddenBy.email,
                createdAt = override.createdAt
            )
        }
    }
}

data class ExtractionWithHistoryResponse(
    val documentId: UUID,
    val documentName: String,
    val slipType: String?,
    val status: ExtractionStatus,
    val currentData: Map<String, Any>?,
    val overrideCount: Int,
    val lastOverrideAt: Instant?,
    val history: List<ExtractionOverrideResponse>?
)

// ==================== Client ====================

data class ClientResponse(
    val id: UUID,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val orderCount: Int,
    val createdAt: Instant?
) {
    companion object {
        fun from(user: User): ClientResponse {
            return ClientResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                phone = user.phone,
                orderCount = user.orders.size,
                createdAt = user.createdAt
            )
        }
    }
}

data class ClientDetailResponse(
    val id: UUID,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val createdAt: Instant?,
    val orders: List<OrderListItemResponse>
)

// ==================== Document for Accountant ====================

data class AccountantDocumentResponse(
    val id: UUID,
    val orderId: UUID,
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long?,
    val mimeType: String?,
    val slipType: String?,
    val uploadedAt: Instant,
    val extractionStatus: ExtractionStatus?,
    val hasExtraction: Boolean
) {
    companion object {
        fun from(document: Document, extractionResult: ExtractionResult?): AccountantDocumentResponse {
            return AccountantDocumentResponse(
                id = document.id!!,
                orderId = document.order.id!!,
                fileName = document.fileName,
                originalFileName = document.originalFileName,
                fileSize = document.fileSize,
                mimeType = document.mimeType,
                slipType = document.slipType,
                uploadedAt = document.uploadedAt,
                extractionStatus = extractionResult?.status,
                hasExtraction = extractionResult != null
            )
        }
    }
}

// ==================== Order Detail for Accountant ====================

data class AccountantOrderDetailResponse(
    val id: UUID,
    val client: ClientResponse,
    val taxYear: Int,
    val status: OrderStatus,
    val notes: String?,
    val createdAt: Instant?,
    val submittedAt: Instant?,
    val filedAt: Instant?,
    val documents: List<AccountantDocumentResponse>,
    val payments: List<PaymentResponse>
)
