package com.taxportal.clientportal.service

import com.taxportal.clientportal.TestUtils
import com.taxportal.clientportal.dto.CreateCheckoutRequest
import com.taxportal.clientportal.entity.Order
import com.taxportal.clientportal.entity.OrderStatus
import com.taxportal.clientportal.entity.Payment
import com.taxportal.clientportal.entity.PaymentStatus
import com.taxportal.clientportal.exception.InvalidOperationException
import com.taxportal.clientportal.exception.ResourceNotFoundException
import com.taxportal.clientportal.repository.PaymentRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.util.*
import kotlin.test.assertEquals

class PaymentServiceTest {

    @MockK
    private lateinit var paymentRepository: PaymentRepository

    @MockK
    private lateinit var orderService: OrderService

    @MockK
    private lateinit var eventPublisher: ApplicationEventPublisher

    private lateinit var paymentService: PaymentService

    private lateinit var testUser: com.taxportal.clientportal.entity.User
    private lateinit var testOrder: Order
    private lateinit var testPayment: Payment

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        testUser = TestUtils.createTestUser()
        testOrder = TestUtils.createTestOrder(user = testUser)
        testPayment = TestUtils.createTestPayment(order = testOrder)

        // Create PaymentService with test configuration (empty API key to skip Stripe init)
        paymentService = PaymentService(
            paymentRepository = paymentRepository,
            orderService = orderService,
            eventPublisher = eventPublisher,
            stripeApiKey = "",
            defaultAmountCents = 9900
        )
    }

    @Test
    fun `createCheckoutSession - throws when order not in OPEN status`() {
        testOrder.status = OrderStatus.SUBMITTED
        val request = CreateCheckoutRequest(
            successUrl = "http://localhost/success",
            cancelUrl = "http://localhost/cancel"
        )

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder

        val exception = assertThrows<InvalidOperationException> {
            paymentService.createCheckoutSession(testUser.id!!, testOrder.id!!, request)
        }

        assertEquals("Payment can only be created for orders in OPEN status", exception.message)
    }

    @Test
    fun `createCheckoutSession - throws when order has no documents`() {
        val request = CreateCheckoutRequest(
            successUrl = "http://localhost/success",
            cancelUrl = "http://localhost/cancel"
        )

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder

        val exception = assertThrows<InvalidOperationException> {
            paymentService.createCheckoutSession(testUser.id!!, testOrder.id!!, request)
        }

        assertEquals("Cannot checkout without uploaded documents", exception.message)
    }

    @Test
    fun `createCheckoutSession - throws when pending payment exists`() {
        val document = TestUtils.createTestDocument(order = testOrder)
        testOrder.documents.add(document)

        val pendingPayment = TestUtils.createTestPayment(order = testOrder, status = PaymentStatus.PENDING)

        val request = CreateCheckoutRequest(
            successUrl = "http://localhost/success",
            cancelUrl = "http://localhost/cancel"
        )

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder
        every { paymentRepository.findByOrderId(testOrder.id!!) } returns listOf(pendingPayment)

        val exception = assertThrows<InvalidOperationException> {
            paymentService.createCheckoutSession(testUser.id!!, testOrder.id!!, request)
        }

        assertEquals("A pending payment already exists for this order", exception.message)
    }

    @Test
    fun `handlePaymentFailure - updates payment status to FAILED`() {
        val sessionId = "cs_test_123"

        every { paymentRepository.findByStripeSessionId(sessionId) } returns testPayment
        every { paymentRepository.save(any<Payment>()) } answers { firstArg() }

        paymentService.handlePaymentFailure(sessionId)

        assertEquals(PaymentStatus.FAILED, testPayment.status)
        verify(exactly = 1) { paymentRepository.save(testPayment) }
    }

    @Test
    fun `handlePaymentFailure - throws when payment not found`() {
        val sessionId = "cs_nonexistent"

        every { paymentRepository.findByStripeSessionId(sessionId) } returns null

        assertThrows<ResourceNotFoundException> {
            paymentService.handlePaymentFailure(sessionId)
        }
    }

    @Test
    fun `handlePaymentSuccess - throws when payment not found`() {
        val sessionId = "cs_nonexistent"

        every { paymentRepository.findByStripeSessionId(sessionId) } returns null

        assertThrows<ResourceNotFoundException> {
            paymentService.handlePaymentSuccess(sessionId)
        }
    }

    @Test
    fun `handlePaymentSuccess - skips when payment already completed`() {
        val sessionId = "cs_test_123"
        testPayment.status = PaymentStatus.COMPLETED

        every { paymentRepository.findByStripeSessionId(sessionId) } returns testPayment

        paymentService.handlePaymentSuccess(sessionId)

        verify(exactly = 0) { paymentRepository.save(any()) }
        verify(exactly = 0) { orderService.updateOrderStatus(any(), any()) }
    }

    @Test
    fun `getPaymentsByOrder - returns payments for order`() {
        val payments = listOf(testPayment)

        every { paymentRepository.findByOrderId(testOrder.id!!) } returns payments

        val result = paymentService.getPaymentsByOrder(testOrder.id!!)

        assertEquals(1, result.size)
        assertEquals(testPayment.id, result[0].id)
    }
}
