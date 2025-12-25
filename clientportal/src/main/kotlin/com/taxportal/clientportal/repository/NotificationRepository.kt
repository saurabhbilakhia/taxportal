package com.taxportal.clientportal.repository

import com.taxportal.clientportal.entity.Notification
import com.taxportal.clientportal.entity.NotificationStatus
import com.taxportal.clientportal.entity.NotificationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {
    fun findByOrderId(orderId: UUID): List<Notification>

    fun findByOrderIdAndType(orderId: UUID, type: NotificationType): List<Notification>

    fun findByStatus(status: NotificationStatus): List<Notification>

    fun existsByOrderIdAndType(orderId: UUID, type: NotificationType): Boolean
}
