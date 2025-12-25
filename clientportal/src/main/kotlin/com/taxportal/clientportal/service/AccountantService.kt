package com.taxportal.clientportal.service

import com.taxportal.clientportal.dto.*
import com.taxportal.clientportal.entity.*
import com.taxportal.clientportal.exception.InvalidOperationException
import com.taxportal.clientportal.exception.ResourceNotFoundException
import com.taxportal.clientportal.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

@Service
class AccountantService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository,
    private val extractionResultRepository: ExtractionResultRepository,
    private val extractionOverrideRepository: ExtractionOverrideRepository,
    private val notificationService: NotificationService
) {

    // ==================== Dashboard ====================

    fun getDashboardStats(): DashboardStatsResponse {
        val allOrders = orderRepository.findAll()
        val now = Instant.now()
        val startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        val startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        val ordersByStatus = allOrders.groupBy { it.status.name }
            .mapValues { it.value.size.toLong() }

        val pendingReview = allOrders.count {
            it.status == OrderStatus.SUBMITTED || it.status == OrderStatus.IN_REVIEW
        }.toLong()

        val filedThisMonth = allOrders.count {
            it.status == OrderStatus.FILED && it.filedAt?.isAfter(startOfMonth) == true
        }.toLong()

        val filedThisYear = allOrders.count {
            it.status == OrderStatus.FILED && it.filedAt?.isAfter(startOfYear) == true
        }.toLong()

        val totalClients = userRepository.findAll().count { it.role == UserRole.CLIENT }.toLong()

        return DashboardStatsResponse(
            totalOrders = allOrders.size.toLong(),
            ordersByStatus = ordersByStatus,
            pendingReview = pendingReview,
            filedThisMonth = filedThisMonth,
            filedThisYear = filedThisYear,
            totalClients = totalClients
        )
    }

    // ==================== Order Management ====================

    fun searchOrders(request: OrderSearchRequest): OrderPageResponse {
        val sort = Sort.by(
            if (request.sortDirection.uppercase() == "ASC") Sort.Direction.ASC else Sort.Direction.DESC,
            request.sortBy
        )
        val pageable = PageRequest.of(request.page, request.size, sort)

        // Get all orders and filter (for simplicity - in production use Specification or QueryDSL)
        var orders = orderRepository.findAll()

        // Apply filters
        request.status?.let { status ->
            orders = orders.filter { it.status == status }
        }

        request.clientEmail?.let { email ->
            orders = orders.filter { it.user.email.contains(email, ignoreCase = true) }
        }

        request.taxYear?.let { year ->
            orders = orders.filter { it.taxYear == year }
        }

        request.fromDate?.let { from ->
            val fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC)
            orders = orders.filter { it.createdAt?.isAfter(fromInstant) == true }
        }

        request.toDate?.let { to ->
            val toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
            orders = orders.filter { it.createdAt?.isBefore(toInstant) == true }
        }

        // Sort
        orders = when (request.sortBy) {
            "createdAt" -> if (request.sortDirection.uppercase() == "ASC")
                orders.sortedBy { it.createdAt } else orders.sortedByDescending { it.createdAt }
            "status" -> if (request.sortDirection.uppercase() == "ASC")
                orders.sortedBy { it.status.name } else orders.sortedByDescending { it.status.name }
            else -> orders.sortedByDescending { it.createdAt }
        }

        val totalElements = orders.size.toLong()
        val totalPages = (totalElements / request.size).toInt() + if (totalElements % request.size > 0) 1 else 0

        // Paginate
        val startIndex = request.page * request.size
        val endIndex = minOf(startIndex + request.size, orders.size)
        val pagedOrders = if (startIndex < orders.size) orders.subList(startIndex, endIndex) else emptyList()

        return OrderPageResponse(
            orders = pagedOrders.map { OrderListItemResponse.from(it) },
            totalElements = totalElements,
            totalPages = totalPages,
            currentPage = request.page,
            pageSize = request.size
        )
    }

    fun getOrderDetail(orderId: UUID): AccountantOrderDetailResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found: $orderId") }

        val documentsWithExtractions = order.documents.map { doc ->
            val extraction = extractionResultRepository.findByDocumentId(doc.id!!)
            AccountantDocumentResponse.from(doc, extraction)
        }

        return AccountantOrderDetailResponse(
            id = order.id!!,
            client = ClientResponse.from(order.user),
            taxYear = order.taxYear,
            status = order.status,
            notes = order.notes,
            createdAt = order.createdAt,
            submittedAt = order.submittedAt,
            filedAt = order.filedAt,
            documents = documentsWithExtractions,
            payments = order.payments.map { PaymentResponse.from(it) }
        )
    }

    @Transactional
    fun updateOrderStatus(orderId: UUID, newStatus: OrderStatus): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found: $orderId") }

        validateStatusTransition(order.status, newStatus)

        order.status = newStatus

        if (newStatus == OrderStatus.FILED) {
            order.filedAt = Instant.now()
            // Send notification to client
            notificationService.sendOrderFiledNotification(order)
        }

        val savedOrder = orderRepository.save(order)
        return OrderResponse.from(savedOrder, includeDetails = true)
    }

    @Transactional
    fun approveOrder(orderId: UUID): OrderResponse {
        return updateOrderStatus(orderId, OrderStatus.PENDING_APPROVAL)
    }

    @Transactional
    fun fileOrder(orderId: UUID): OrderResponse {
        return updateOrderStatus(orderId, OrderStatus.FILED)
    }

    @Transactional
    fun bulkUpdateStatus(request: BulkStatusUpdateRequest): BulkStatusUpdateResponse {
        val failures = mutableListOf<BulkUpdateFailure>()
        var successCount = 0

        for (orderId in request.orderIds) {
            try {
                updateOrderStatus(orderId, request.status)
                successCount++
            } catch (e: Exception) {
                failures.add(BulkUpdateFailure(orderId, e.message ?: "Unknown error"))
            }
        }

        return BulkStatusUpdateResponse(
            successCount = successCount,
            failedCount = failures.size,
            failures = failures
        )
    }

    private fun validateStatusTransition(currentStatus: OrderStatus, newStatus: OrderStatus) {
        val validTransitions = mapOf(
            OrderStatus.OPEN to setOf(OrderStatus.SUBMITTED, OrderStatus.CANCELLED),
            OrderStatus.SUBMITTED to setOf(OrderStatus.IN_REVIEW, OrderStatus.CANCELLED),
            OrderStatus.IN_REVIEW to setOf(OrderStatus.PENDING_APPROVAL, OrderStatus.SUBMITTED),
            OrderStatus.PENDING_APPROVAL to setOf(OrderStatus.FILED, OrderStatus.IN_REVIEW)
        )

        val allowedStatuses = validTransitions[currentStatus] ?: emptySet()
        if (newStatus !in allowedStatuses) {
            throw InvalidOperationException("Cannot transition from $currentStatus to $newStatus")
        }
    }

    // ==================== Documents & Extraction ====================

    fun getOrderDocuments(orderId: UUID): List<AccountantDocumentResponse> {
        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found: $orderId") }

        return order.documents.map { doc ->
            val extraction = extractionResultRepository.findByDocumentId(doc.id!!)
            AccountantDocumentResponse.from(doc, extraction)
        }
    }

    fun getDocumentExtraction(documentId: UUID): ExtractionWithHistoryResponse {
        val document = documentRepository.findById(documentId)
            .orElseThrow { ResourceNotFoundException("Document not found: $documentId") }

        val extraction = extractionResultRepository.findByDocumentId(documentId)
            ?: throw ResourceNotFoundException("Extraction not found for document: $documentId")

        val overrides = extractionOverrideRepository.findByExtractionResultIdOrderByCreatedAtDesc(extraction.id!!)

        return ExtractionWithHistoryResponse(
            documentId = documentId,
            documentName = document.originalFileName,
            slipType = document.slipType,
            status = extraction.status,
            currentData = extraction.extractedData,
            overrideCount = overrides.size,
            lastOverrideAt = overrides.firstOrNull()?.createdAt,
            history = overrides.map { ExtractionOverrideResponse.from(it) }
        )
    }

    @Transactional
    fun overrideExtraction(
        documentId: UUID,
        request: ExtractionOverrideRequest,
        accountantId: UUID
    ): ExtractionOverrideResponse {
        val extraction = extractionResultRepository.findByDocumentId(documentId)
            ?: throw ResourceNotFoundException("Extraction not found for document: $documentId")

        val accountant = userRepository.findById(accountantId)
            .orElseThrow { ResourceNotFoundException("User not found: $accountantId") }

        // Store the override in audit trail
        val override = ExtractionOverride(
            extractionResult = extraction,
            previousData = extraction.extractedData ?: emptyMap(),
            newData = request.extractedData,
            overrideReason = request.reason,
            overriddenBy = accountant
        )
        val savedOverride = extractionOverrideRepository.save(override)

        // Update the extraction result
        extraction.extractedData = request.extractedData
        extraction.updatedAt = Instant.now()
        extractionResultRepository.save(extraction)

        return ExtractionOverrideResponse.from(savedOverride)
    }

    fun getExtractionHistory(documentId: UUID): List<ExtractionOverrideResponse> {
        val extraction = extractionResultRepository.findByDocumentId(documentId)
            ?: throw ResourceNotFoundException("Extraction not found for document: $documentId")

        return extractionOverrideRepository.findByExtractionResultIdOrderByCreatedAtDesc(extraction.id!!)
            .map { ExtractionOverrideResponse.from(it) }
    }

    // ==================== Clients ====================

    fun getClients(): List<ClientResponse> {
        return userRepository.findAll()
            .filter { it.role == UserRole.CLIENT }
            .map { ClientResponse.from(it) }
    }

    fun getClientDetail(clientId: UUID): ClientDetailResponse {
        val client = userRepository.findById(clientId)
            .orElseThrow { ResourceNotFoundException("Client not found: $clientId") }

        if (client.role != UserRole.CLIENT) {
            throw ResourceNotFoundException("Client not found: $clientId")
        }

        val orders = orderRepository.findByUserId(clientId)
            .map { OrderListItemResponse.from(it) }

        return ClientDetailResponse(
            id = client.id!!,
            email = client.email,
            firstName = client.firstName,
            lastName = client.lastName,
            phone = client.phone,
            createdAt = client.createdAt,
            orders = orders
        )
    }
}
