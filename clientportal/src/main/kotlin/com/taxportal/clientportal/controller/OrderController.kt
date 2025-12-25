package com.taxportal.clientportal.controller

import com.taxportal.clientportal.dto.*
import com.taxportal.clientportal.entity.OrderStatus
import com.taxportal.clientportal.repository.UserRepository
import com.taxportal.clientportal.service.DocumentService
import com.taxportal.clientportal.service.ExtractionService
import com.taxportal.clientportal.service.OrderService
import com.taxportal.clientportal.service.PaymentService
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
    private val documentService: DocumentService,
    private val paymentService: PaymentService,
    private val extractionService: ExtractionService,
    private val userRepository: UserRepository
) {

    // ==================== Order Endpoints ====================

    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: CreateOrderRequest
    ): ResponseEntity<OrderResponse> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val response = orderService.createOrder(user, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun listOrders(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) status: OrderStatus?
    ): ResponseEntity<OrderListResponse> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val orders = orderService.getOrders(user.id!!, status)
        return ResponseEntity.ok(OrderListResponse(orders = orders, total = orders.size))
    }

    @GetMapping("/{orderId}")
    fun getOrderDetails(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID
    ): ResponseEntity<OrderResponse> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val response = orderService.getOrderDetails(user.id!!, orderId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{orderId}/submit")
    fun submitOrder(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID
    ): ResponseEntity<OrderResponse> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val response = orderService.submitOrder(user.id!!, orderId)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{orderId}")
    fun cancelOrder(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID
    ): ResponseEntity<Void> {
        val user = userRepository.findByEmail(userDetails.username)!!
        orderService.cancelOrder(user.id!!, orderId)
        return ResponseEntity.noContent().build()
    }

    // ==================== Document Endpoints ====================

    @PostMapping("/{orderId}/documents", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadDocument(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(name = "slip_type", required = false) slipType: String?
    ): ResponseEntity<DocumentUploadResponse> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val response = documentService.uploadDocument(user.id!!, orderId, file, slipType)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{orderId}/documents")
    fun listDocuments(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID
    ): ResponseEntity<List<DocumentResponse>> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val documents = documentService.getDocuments(user.id!!, orderId)
        return ResponseEntity.ok(documents)
    }

    @GetMapping("/{orderId}/documents/{documentId}")
    fun getDocument(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID,
        @PathVariable documentId: UUID
    ): ResponseEntity<DocumentResponse> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val document = documentService.getDocument(user.id!!, orderId, documentId)
        return ResponseEntity.ok(document)
    }

    @GetMapping("/{orderId}/documents/{documentId}/download")
    fun downloadDocument(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID,
        @PathVariable documentId: UUID
    ): ResponseEntity<Resource> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val (resource, document) = documentService.downloadDocument(user.id!!, orderId, documentId)

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(document.mimeType ?: "application/octet-stream"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${document.originalFileName}\"")
            .body(resource)
    }

    @DeleteMapping("/{orderId}/documents/{documentId}")
    fun deleteDocument(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID,
        @PathVariable documentId: UUID
    ): ResponseEntity<Void> {
        val user = userRepository.findByEmail(userDetails.username)!!
        documentService.deleteDocument(user.id!!, orderId, documentId)
        return ResponseEntity.noContent().build()
    }

    // ==================== Payment Endpoints ====================

    @PostMapping("/{orderId}/pay")
    fun createCheckoutSession(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID,
        @Valid @RequestBody request: CreateCheckoutRequest
    ): ResponseEntity<CheckoutResponse> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val response = paymentService.createCheckoutSession(user.id!!, orderId, request)
        return ResponseEntity.ok(response)
    }

    // ==================== Extraction Endpoints ====================

    @GetMapping("/{orderId}/extractions")
    fun getExtractionResults(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID
    ): ResponseEntity<OrderExtractionResponse> {
        val user = userRepository.findByEmail(userDetails.username)!!
        val userId = user.id!!
        orderService.getOrderEntity(userId, orderId) // Verify ownership
        val response = extractionService.getOrderExtractions(userId, orderId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{orderId}/extractions/{documentId}/retry")
    fun retryExtraction(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID,
        @PathVariable documentId: UUID
    ): ResponseEntity<ExtractionResultResponse> {
        val user = userRepository.findByEmail(userDetails.username)!!
        orderService.getOrderEntity(user.id!!, orderId) // Verify ownership
        val response = extractionService.retryFailedExtraction(orderId, documentId)
        return ResponseEntity.ok(response)
    }
}
