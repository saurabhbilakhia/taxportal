package com.taxportal.clientportal.service

import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.taxportal.clientportal.dto.CheckoutResponse
import com.taxportal.clientportal.dto.CreateCheckoutRequest
import com.taxportal.clientportal.entity.Order
import com.taxportal.clientportal.entity.OrderStatus
import com.taxportal.clientportal.entity.Payment
import com.taxportal.clientportal.entity.PaymentStatus
import com.taxportal.clientportal.event.PaymentCompletedEvent
import com.taxportal.clientportal.exception.InvalidOperationException
import com.taxportal.clientportal.exception.PaymentException
import com.taxportal.clientportal.exception.ResourceNotFoundException
import com.taxportal.clientportal.repository.PaymentRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderService: OrderService,
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${stripe.api-key:}")
    private val stripeApiKey: String,
    @Value("\${app.payment.amount-cents:9900}")
    private val defaultAmountCents: Int
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        if (stripeApiKey.isNotBlank() && !stripeApiKey.startsWith("sk_test_placeholder")) {
            Stripe.apiKey = stripeApiKey
        }
    }

    @Transactional
    fun createCheckoutSession(
        userId: UUID,
        orderId: UUID,
        request: CreateCheckoutRequest
    ): CheckoutResponse {
        val order = orderService.getOrderEntity(userId, orderId)

        if (order.status != OrderStatus.OPEN) {
            throw InvalidOperationException("Payment can only be created for orders in OPEN status")
        }

        if (order.documents.isEmpty()) {
            throw InvalidOperationException("Cannot checkout without uploaded documents")
        }

        val existingPendingPayment = paymentRepository.findByOrderId(orderId)
            .find { it.status == PaymentStatus.PENDING }

        if (existingPendingPayment != null) {
            throw InvalidOperationException("A pending payment already exists for this order")
        }

        try {
            val sessionParams = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.successUrl)
                .setCancelUrl(request.cancelUrl)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("cad")
                                .setUnitAmount(defaultAmountCents.toLong())
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Tax Filing Service - ${order.taxYear}")
                                        .setDescription("Tax return filing for year ${order.taxYear}")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .putMetadata("order_id", orderId.toString())
                .putMetadata("user_id", userId.toString())
                .build()

            val session = Session.create(sessionParams)

            val payment = Payment(
                order = order,
                stripeSessionId = session.id,
                amountCents = defaultAmountCents,
                currency = "CAD"
            )
            paymentRepository.save(payment)

            return CheckoutResponse(
                checkoutUrl = session.url,
                sessionId = session.id
            )
        } catch (e: Exception) {
            logger.error("Failed to create Stripe checkout session", e)
            throw PaymentException("Failed to create checkout session: ${e.message}", e)
        }
    }

    @Transactional
    fun handlePaymentSuccess(sessionId: String) {
        val payment = paymentRepository.findByStripeSessionId(sessionId)
            ?: throw ResourceNotFoundException("Payment not found for session: $sessionId")

        if (payment.status == PaymentStatus.COMPLETED) {
            logger.info("Payment already completed for session: $sessionId")
            return
        }

        try {
            val session = Session.retrieve(sessionId)
            payment.stripePaymentIntentId = session.paymentIntent
            payment.status = PaymentStatus.COMPLETED
            payment.paidAt = Instant.now()
            paymentRepository.save(payment)

            orderService.updateOrderStatus(payment.order.id!!, OrderStatus.SUBMITTED)

            logger.info("Payment completed for order: ${payment.order.id}")

            // Publish event to trigger extraction workflow
            eventPublisher.publishEvent(PaymentCompletedEvent(
                orderId = payment.order.id!!,
                paymentId = payment.id!!
            ))
        } catch (e: Exception) {
            logger.error("Failed to process payment success for session: $sessionId", e)
            throw PaymentException("Failed to process payment: ${e.message}", e)
        }
    }

    @Transactional
    fun handlePaymentFailure(sessionId: String) {
        val payment = paymentRepository.findByStripeSessionId(sessionId)
            ?: throw ResourceNotFoundException("Payment not found for session: $sessionId")

        payment.status = PaymentStatus.FAILED
        paymentRepository.save(payment)

        logger.info("Payment failed for order: ${payment.order.id}")
    }

    fun getPaymentsByOrder(orderId: UUID): List<Payment> {
        return paymentRepository.findByOrderId(orderId)
    }
}
