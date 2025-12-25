package com.taxportal.clientportal.repository

import com.taxportal.clientportal.entity.Order
import com.taxportal.clientportal.entity.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByUserId(userId: UUID): List<Order>

    fun findByUserIdAndStatus(userId: UUID, status: OrderStatus): List<Order>

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<Order>

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    fun findByUserIdAndStatusOrderByCreatedAtDesc(userId: UUID, status: OrderStatus): List<Order>

    fun findByIdAndUserId(id: UUID, userId: UUID): Order?
}
