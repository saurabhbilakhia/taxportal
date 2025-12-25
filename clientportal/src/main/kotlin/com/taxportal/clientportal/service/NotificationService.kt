package com.taxportal.clientportal.service

import com.taxportal.clientportal.entity.*
import com.taxportal.clientportal.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val mailSender: JavaMailSender?,
    @Value("\${notification.accountant-email:}")
    private val accountantEmail: String,
    @Value("\${notification.from-email:noreply@taxportal.com}")
    private val fromEmail: String,
    @Value("\${notification.enabled:true}")
    private val notificationsEnabled: Boolean
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async("notificationExecutor")
    @Transactional
    fun sendReviewReadyNotification(order: Order) {
        if (accountantEmail.isBlank()) {
            logger.warn("Accountant email not configured, skipping notification")
            return
        }

        // Check if notification already sent
        if (notificationRepository.existsByOrderIdAndType(order.id!!, NotificationType.REVIEW_READY)) {
            logger.info("Review notification already sent for order: ${order.id}")
            return
        }

        val subject = "Tax Order Ready for Review - ${order.taxYear}"
        val message = buildReviewReadyMessage(order)

        val notification = Notification(
            order = order,
            type = NotificationType.REVIEW_READY,
            recipientEmail = accountantEmail,
            subject = subject,
            message = message
        )

        val savedNotification = notificationRepository.save(notification)

        // Send email
        if (notificationsEnabled && mailSender != null) {
            try {
                sendEmail(accountantEmail, subject, message)
                savedNotification.status = NotificationStatus.SENT
                savedNotification.sentAt = Instant.now()
                logger.info("Review notification sent for order: ${order.id}")
            } catch (e: Exception) {
                logger.error("Failed to send email notification for order: ${order.id}", e)
                savedNotification.status = NotificationStatus.FAILED
            }
            notificationRepository.save(savedNotification)
        } else {
            logger.info("Email sending disabled, notification stored in database only")
            savedNotification.status = NotificationStatus.SENT
            savedNotification.sentAt = Instant.now()
            notificationRepository.save(savedNotification)
        }
    }

    @Async("notificationExecutor")
    @Transactional
    fun sendOrderFiledNotification(order: Order) {
        val clientEmail = order.user.email

        val subject = "Your Tax Return Has Been Filed - ${order.taxYear}"
        val message = buildOrderFiledMessage(order)

        val notification = Notification(
            order = order,
            type = NotificationType.ORDER_FILED,
            recipientEmail = clientEmail,
            subject = subject,
            message = message
        )

        val savedNotification = notificationRepository.save(notification)

        if (notificationsEnabled && mailSender != null) {
            try {
                sendEmail(clientEmail, subject, message)
                savedNotification.status = NotificationStatus.SENT
                savedNotification.sentAt = Instant.now()
            } catch (e: Exception) {
                logger.error("Failed to send filed notification for order: ${order.id}", e)
                savedNotification.status = NotificationStatus.FAILED
            }
            notificationRepository.save(savedNotification)
        }
    }

    fun getNotificationsForOrder(orderId: UUID): List<Notification> {
        return notificationRepository.findByOrderId(orderId)
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        val message = SimpleMailMessage().apply {
            setFrom(fromEmail)
            setTo(to)
            setSubject(subject)
            setText(body)
        }
        mailSender?.send(message)
    }

    private fun buildReviewReadyMessage(order: Order): String {
        val clientName = listOfNotNull(order.user.firstName, order.user.lastName)
            .joinToString(" ")
            .ifBlank { order.user.email }

        return """
            |A new tax order is ready for review.
            |
            |Order Details:
            |- Order ID: ${order.id}
            |- Tax Year: ${order.taxYear}
            |- Client: $clientName
            |- Client Email: ${order.user.email}
            |- Documents: ${order.documents.size}
            |- Submitted: ${order.submittedAt}
            |
            |Please review the extracted data and process this order.
            |
            |Notes from client:
            |${order.notes ?: "No additional notes"}
        """.trimMargin()
    }

    private fun buildOrderFiledMessage(order: Order): String {
        val clientName = order.user.firstName ?: "Valued Customer"

        return """
            |Dear $clientName,
            |
            |Great news! Your tax return for ${order.taxYear} has been successfully filed.
            |
            |Order Details:
            |- Order ID: ${order.id}
            |- Tax Year: ${order.taxYear}
            |- Filed Date: ${order.filedAt}
            |
            |Thank you for choosing our tax filing service.
            |
            |If you have any questions, please don't hesitate to contact us.
            |
            |Best regards,
            |Tax Portal Team
        """.trimMargin()
    }
}
