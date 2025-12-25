package com.taxportal.clientportal

import com.taxportal.clientportal.entity.*
import java.time.Instant
import java.util.*

object TestUtils {

    const val TEST_EMAIL = "test@example.com"
    const val TEST_PASSWORD = "password123"
    const val TEST_FIRST_NAME = "John"
    const val TEST_LAST_NAME = "Doe"
    const val TEST_PHONE = "555-1234"
    const val TEST_TOKEN = "test.jwt.token"
    const val TEST_TAX_YEAR = 2024

    fun createTestUser(
        id: UUID = UUID.randomUUID(),
        email: String = TEST_EMAIL,
        passwordHash: String = "hashedPassword",
        firstName: String? = TEST_FIRST_NAME,
        lastName: String? = TEST_LAST_NAME,
        phone: String? = TEST_PHONE,
        role: UserRole = UserRole.CLIENT
    ): User {
        return User(
            id = id,
            email = email,
            passwordHash = passwordHash,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            role = role
        )
    }

    fun createTestOrder(
        id: UUID = UUID.randomUUID(),
        user: User = createTestUser(),
        status: OrderStatus = OrderStatus.OPEN,
        taxYear: Int = TEST_TAX_YEAR,
        notes: String? = null
    ): Order {
        return Order(
            id = id,
            user = user,
            status = status,
            taxYear = taxYear,
            notes = notes
        )
    }

    fun createTestDocument(
        id: UUID = UUID.randomUUID(),
        order: Order = createTestOrder(),
        fileName: String = "document.pdf",
        originalFileName: String = "original.pdf",
        filePath: String = "/uploads/test/document.pdf",
        fileSize: Long = 1024L,
        mimeType: String = "application/pdf",
        slipType: String? = "T4"
    ): Document {
        return Document(
            id = id,
            order = order,
            fileName = fileName,
            originalFileName = originalFileName,
            filePath = filePath,
            fileSize = fileSize,
            mimeType = mimeType,
            slipType = slipType
        )
    }

    fun createTestPayment(
        id: UUID = UUID.randomUUID(),
        order: Order = createTestOrder(),
        stripeSessionId: String = "cs_test_123",
        amountCents: Int = 9900,
        currency: String = "CAD",
        status: PaymentStatus = PaymentStatus.PENDING
    ): Payment {
        return Payment(
            id = id,
            order = order,
            stripeSessionId = stripeSessionId,
            amountCents = amountCents,
            currency = currency,
            status = status
        )
    }
}
