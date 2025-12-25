package com.taxportal.clientportal.service

import com.taxportal.clientportal.TestUtils
import com.taxportal.clientportal.dto.CreateOrderRequest
import com.taxportal.clientportal.entity.Document
import com.taxportal.clientportal.entity.Order
import com.taxportal.clientportal.entity.OrderStatus
import com.taxportal.clientportal.exception.InvalidOperationException
import com.taxportal.clientportal.exception.ResourceNotFoundException
import com.taxportal.clientportal.repository.OrderRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrderServiceTest {

    @MockK
    private lateinit var orderRepository: OrderRepository

    @MockK
    private lateinit var storageService: StorageService

    @InjectMockKs
    private lateinit var orderService: OrderService

    private lateinit var testUser: com.taxportal.clientportal.entity.User
    private lateinit var testOrder: Order

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        testUser = TestUtils.createTestUser()
        testOrder = TestUtils.createTestOrder(user = testUser)
    }

    @Test
    fun `createOrder - creates order successfully`() {
        val request = CreateOrderRequest(taxYear = 2024, notes = "Test notes")
        val savedOrder = TestUtils.createTestOrder(user = testUser, taxYear = 2024, notes = "Test notes")

        every { orderRepository.save(any<Order>()) } returns savedOrder

        val result = orderService.createOrder(testUser, request)

        assertNotNull(result)
        assertEquals(2024, result.taxYear)
        assertEquals(OrderStatus.OPEN, result.status)

        verify(exactly = 1) { orderRepository.save(any<Order>()) }
    }

    @Test
    fun `getOrders - returns orders for user`() {
        val orders = listOf(
            TestUtils.createTestOrder(user = testUser, taxYear = 2024),
            TestUtils.createTestOrder(user = testUser, taxYear = 2023)
        )

        every { orderRepository.findByUserIdOrderByCreatedAtDesc(testUser.id!!) } returns orders

        val result = orderService.getOrders(testUser.id!!, null)

        assertEquals(2, result.size)
        verify(exactly = 1) { orderRepository.findByUserIdOrderByCreatedAtDesc(testUser.id!!) }
    }

    @Test
    fun `getOrders - filters by status when provided`() {
        val orders = listOf(TestUtils.createTestOrder(user = testUser, status = OrderStatus.SUBMITTED))

        every {
            orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(testUser.id!!, OrderStatus.SUBMITTED)
        } returns orders

        val result = orderService.getOrders(testUser.id!!, OrderStatus.SUBMITTED)

        assertEquals(1, result.size)
        verify(exactly = 1) {
            orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(testUser.id!!, OrderStatus.SUBMITTED)
        }
    }

    @Test
    fun `getOrderDetails - returns order details`() {
        every { orderRepository.findByIdAndUserId(testOrder.id!!, testUser.id!!) } returns testOrder

        val result = orderService.getOrderDetails(testUser.id!!, testOrder.id!!)

        assertNotNull(result)
        assertEquals(testOrder.id, result.id)

        verify(exactly = 1) { orderRepository.findByIdAndUserId(testOrder.id!!, testUser.id!!) }
    }

    @Test
    fun `getOrderDetails - throws ResourceNotFoundException when order not found`() {
        val orderId = UUID.randomUUID()
        every { orderRepository.findByIdAndUserId(orderId, testUser.id!!) } returns null

        assertThrows<ResourceNotFoundException> {
            orderService.getOrderDetails(testUser.id!!, orderId)
        }
    }

    @Test
    fun `submitOrder - submits order when in OPEN status with documents`() {
        val document = TestUtils.createTestDocument(order = testOrder)
        testOrder.documents.add(document)

        every { orderRepository.findByIdAndUserId(testOrder.id!!, testUser.id!!) } returns testOrder
        every { orderRepository.save(any<Order>()) } answers { firstArg() }

        val result = orderService.submitOrder(testUser.id!!, testOrder.id!!)

        assertEquals(OrderStatus.SUBMITTED, result.status)
        assertNotNull(result.submittedAt)

        verify(exactly = 1) { orderRepository.save(any<Order>()) }
    }

    @Test
    fun `submitOrder - throws when order not in OPEN status`() {
        testOrder.status = OrderStatus.SUBMITTED

        every { orderRepository.findByIdAndUserId(testOrder.id!!, testUser.id!!) } returns testOrder

        val exception = assertThrows<InvalidOperationException> {
            orderService.submitOrder(testUser.id!!, testOrder.id!!)
        }

        assertEquals("Order can only be submitted when in OPEN status", exception.message)
    }

    @Test
    fun `submitOrder - throws when order has no documents`() {
        every { orderRepository.findByIdAndUserId(testOrder.id!!, testUser.id!!) } returns testOrder

        val exception = assertThrows<InvalidOperationException> {
            orderService.submitOrder(testUser.id!!, testOrder.id!!)
        }

        assertEquals("Cannot submit order without documents", exception.message)
    }

    @Test
    fun `cancelOrder - cancels order in OPEN status`() {
        every { orderRepository.findByIdAndUserId(testOrder.id!!, testUser.id!!) } returns testOrder
        every { orderRepository.save(any<Order>()) } answers { firstArg() }
        every { storageService.deleteOrderDirectory(testOrder.id!!) } just runs

        orderService.cancelOrder(testUser.id!!, testOrder.id!!)

        assertEquals(OrderStatus.CANCELLED, testOrder.status)
        verify(exactly = 1) { storageService.deleteOrderDirectory(testOrder.id!!) }
    }

    @Test
    fun `cancelOrder - throws when order not in OPEN status`() {
        testOrder.status = OrderStatus.SUBMITTED

        every { orderRepository.findByIdAndUserId(testOrder.id!!, testUser.id!!) } returns testOrder

        val exception = assertThrows<InvalidOperationException> {
            orderService.cancelOrder(testUser.id!!, testOrder.id!!)
        }

        assertEquals("Only orders in OPEN status can be cancelled", exception.message)
    }

    @Test
    fun `updateOrderStatus - updates status with valid transition`() {
        testOrder.status = OrderStatus.SUBMITTED

        every { orderRepository.findById(testOrder.id!!) } returns Optional.of(testOrder)
        every { orderRepository.save(any<Order>()) } answers { firstArg() }

        val result = orderService.updateOrderStatus(testOrder.id!!, OrderStatus.IN_REVIEW)

        assertEquals(OrderStatus.IN_REVIEW, result.status)
    }

    @Test
    fun `updateOrderStatus - throws on invalid transition`() {
        testOrder.status = OrderStatus.OPEN

        every { orderRepository.findById(testOrder.id!!) } returns Optional.of(testOrder)

        assertThrows<InvalidOperationException> {
            orderService.updateOrderStatus(testOrder.id!!, OrderStatus.FILED)
        }
    }

    @Test
    fun `updateOrderStatus - sets filedAt when transitioning to FILED`() {
        testOrder.status = OrderStatus.PENDING_APPROVAL

        every { orderRepository.findById(testOrder.id!!) } returns Optional.of(testOrder)
        every { orderRepository.save(any<Order>()) } answers { firstArg() }

        val result = orderService.updateOrderStatus(testOrder.id!!, OrderStatus.FILED)

        assertEquals(OrderStatus.FILED, result.status)
        assertNotNull(result.filedAt)
    }
}
