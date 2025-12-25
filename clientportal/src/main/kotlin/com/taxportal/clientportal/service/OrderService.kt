package com.taxportal.clientportal.service

import com.taxportal.clientportal.dto.CreateOrderRequest
import com.taxportal.clientportal.dto.OrderResponse
import com.taxportal.clientportal.entity.Order
import com.taxportal.clientportal.entity.OrderStatus
import com.taxportal.clientportal.entity.User
import com.taxportal.clientportal.exception.InvalidOperationException
import com.taxportal.clientportal.exception.ResourceNotFoundException
import com.taxportal.clientportal.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val storageService: StorageService
) {

    @Transactional
    fun createOrder(user: User, request: CreateOrderRequest): OrderResponse {
        val order = Order(
            user = user,
            taxYear = request.taxYear,
            notes = request.notes
        )
        val savedOrder = orderRepository.save(order)
        return OrderResponse.from(savedOrder)
    }

    fun getOrders(userId: UUID, status: OrderStatus?): List<OrderResponse> {
        val orders = if (status != null) {
            orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
        } else {
            orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
        }
        return orders.map { OrderResponse.from(it) }
    }

    fun getOrderDetails(userId: UUID, orderId: UUID): OrderResponse {
        val order = orderRepository.findByIdAndUserId(orderId, userId)
            ?: throw ResourceNotFoundException("Order not found")
        return OrderResponse.from(order, includeDetails = true)
    }

    fun getOrderEntity(userId: UUID, orderId: UUID): Order {
        return orderRepository.findByIdAndUserId(orderId, userId)
            ?: throw ResourceNotFoundException("Order not found")
    }

    @Transactional
    fun submitOrder(userId: UUID, orderId: UUID): OrderResponse {
        val order = getOrderEntity(userId, orderId)

        if (order.status != OrderStatus.OPEN) {
            throw InvalidOperationException("Order can only be submitted when in OPEN status")
        }

        if (order.documents.isEmpty()) {
            throw InvalidOperationException("Cannot submit order without documents")
        }

        order.status = OrderStatus.SUBMITTED
        order.submittedAt = Instant.now()

        val savedOrder = orderRepository.save(order)
        return OrderResponse.from(savedOrder, includeDetails = true)
    }

    @Transactional
    fun cancelOrder(userId: UUID, orderId: UUID) {
        val order = getOrderEntity(userId, orderId)

        if (order.status != OrderStatus.OPEN) {
            throw InvalidOperationException("Only orders in OPEN status can be cancelled")
        }

        order.status = OrderStatus.CANCELLED
        orderRepository.save(order)

        storageService.deleteOrderDirectory(orderId)
    }

    @Transactional
    fun updateOrderStatus(orderId: UUID, newStatus: OrderStatus): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found") }

        validateStatusTransition(order.status, newStatus)

        order.status = newStatus

        if (newStatus == OrderStatus.FILED) {
            order.filedAt = Instant.now()
        }

        return orderRepository.save(order)
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
}
