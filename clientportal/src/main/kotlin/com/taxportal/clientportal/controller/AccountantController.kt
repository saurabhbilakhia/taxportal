package com.taxportal.clientportal.controller

import com.taxportal.clientportal.dto.*
import com.taxportal.clientportal.entity.OrderStatus
import com.taxportal.clientportal.service.AccountantService
import com.taxportal.clientportal.service.ExtractionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/v1/accountant")
class AccountantController(
    private val accountantService: AccountantService,
    private val extractionService: ExtractionService
) {

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    fun getDashboardStats(): ResponseEntity<DashboardStatsResponse> {
        val stats = accountantService.getDashboardStats()
        return ResponseEntity.ok(stats)
    }

    // ==================== Orders ====================

    @GetMapping("/orders")
    fun getOrders(
        @RequestParam(required = false) status: OrderStatus?,
        @RequestParam(required = false) clientEmail: String?,
        @RequestParam(required = false) taxYear: Int?,
        @RequestParam(required = false) fromDate: LocalDate?,
        @RequestParam(required = false) toDate: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<OrderPageResponse> {
        val request = OrderSearchRequest(
            status = status,
            clientEmail = clientEmail,
            taxYear = taxYear,
            fromDate = fromDate,
            toDate = toDate,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val orders = accountantService.searchOrders(request)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/orders/{orderId}")
    fun getOrderDetail(@PathVariable orderId: UUID): ResponseEntity<AccountantOrderDetailResponse> {
        val order = accountantService.getOrderDetail(orderId)
        return ResponseEntity.ok(order)
    }

    @PatchMapping("/orders/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable orderId: UUID,
        @Valid @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderResponse> {
        val order = accountantService.updateOrderStatus(orderId, request.status)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/orders/{orderId}/extractions")
    fun getOrderExtractions(@PathVariable orderId: UUID): ResponseEntity<OrderExtractionResponse> {
        val extractions = extractionService.getOrderExtractionsForAccountant(orderId)
        return ResponseEntity.ok(extractions)
    }

    @PostMapping("/orders/{orderId}/extractions/{documentId}/retry")
    fun retryExtraction(
        @PathVariable orderId: UUID,
        @PathVariable documentId: UUID
    ): ResponseEntity<ExtractionResultResponse> {
        val response = extractionService.retryFailedExtraction(orderId, documentId)
        return ResponseEntity.ok(response)
    }

    // ==================== Clients ====================

    @GetMapping("/clients")
    fun getClients(): ResponseEntity<List<ClientResponse>> {
        val clients = accountantService.getClients()
        return ResponseEntity.ok(clients)
    }

    @GetMapping("/clients/{clientId}")
    fun getClientDetail(@PathVariable clientId: UUID): ResponseEntity<ClientDetailResponse> {
        val client = accountantService.getClientDetail(clientId)
        return ResponseEntity.ok(client)
    }
}
