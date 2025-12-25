package com.taxportal.clientportal.service

import com.taxportal.clientportal.TestUtils
import com.taxportal.clientportal.entity.Document
import com.taxportal.clientportal.entity.Order
import com.taxportal.clientportal.entity.OrderStatus
import com.taxportal.clientportal.exception.BadRequestException
import com.taxportal.clientportal.exception.InvalidOperationException
import com.taxportal.clientportal.exception.ResourceNotFoundException
import com.taxportal.clientportal.repository.DocumentRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.ByteArrayResource
import org.springframework.web.multipart.MultipartFile
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DocumentServiceTest {

    @MockK
    private lateinit var documentRepository: DocumentRepository

    @MockK
    private lateinit var orderService: OrderService

    @MockK
    private lateinit var storageService: StorageService

    @InjectMockKs
    private lateinit var documentService: DocumentService

    private lateinit var testUser: com.taxportal.clientportal.entity.User
    private lateinit var testOrder: Order
    private lateinit var testDocument: Document

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        testUser = TestUtils.createTestUser()
        testOrder = TestUtils.createTestOrder(user = testUser)
        testDocument = TestUtils.createTestDocument(order = testOrder)
    }

    @Test
    fun `uploadDocument - uploads document successfully`() {
        val mockFile = mockk<MultipartFile>()
        every { mockFile.isEmpty } returns false
        every { mockFile.size } returns 1024L
        every { mockFile.contentType } returns "application/pdf"
        every { mockFile.originalFilename } returns "test.pdf"

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder
        every { storageService.store(mockFile, testOrder.id!!) } returns "/uploads/${testOrder.id}/stored.pdf"
        every { documentRepository.save(any<Document>()) } answers {
            val doc = firstArg<Document>()
            Document(
                id = UUID.randomUUID(),
                order = doc.order,
                fileName = doc.fileName,
                originalFileName = doc.originalFileName,
                filePath = doc.filePath,
                fileSize = doc.fileSize,
                mimeType = doc.mimeType,
                slipType = doc.slipType
            )
        }

        val result = documentService.uploadDocument(testUser.id!!, testOrder.id!!, mockFile, "T4")

        assertNotNull(result)
        assertEquals("test.pdf", result.fileName)
        assertEquals("T4", result.slipType)

        verify(exactly = 1) { storageService.store(mockFile, testOrder.id!!) }
        verify(exactly = 1) { documentRepository.save(any<Document>()) }
    }

    @Test
    fun `uploadDocument - throws when order not in OPEN status`() {
        testOrder.status = OrderStatus.SUBMITTED
        val mockFile = mockk<MultipartFile>()

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder

        val exception = assertThrows<InvalidOperationException> {
            documentService.uploadDocument(testUser.id!!, testOrder.id!!, mockFile, null)
        }

        assertEquals("Documents can only be uploaded to orders in OPEN status", exception.message)
    }

    @Test
    fun `uploadDocument - throws when file is empty`() {
        val mockFile = mockk<MultipartFile>()
        every { mockFile.isEmpty } returns true

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder

        val exception = assertThrows<BadRequestException> {
            documentService.uploadDocument(testUser.id!!, testOrder.id!!, mockFile, null)
        }

        assertEquals("File is empty", exception.message)
    }

    @Test
    fun `uploadDocument - throws when file exceeds size limit`() {
        val mockFile = mockk<MultipartFile>()
        every { mockFile.isEmpty } returns false
        every { mockFile.size } returns 20 * 1024 * 1024L // 20MB

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder

        val exception = assertThrows<BadRequestException> {
            documentService.uploadDocument(testUser.id!!, testOrder.id!!, mockFile, null)
        }

        assertEquals("File size exceeds maximum allowed size of 10MB", exception.message)
    }

    @Test
    fun `uploadDocument - throws when file type is invalid`() {
        val mockFile = mockk<MultipartFile>()
        every { mockFile.isEmpty } returns false
        every { mockFile.size } returns 1024L
        every { mockFile.contentType } returns "application/zip"

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder

        val exception = assertThrows<BadRequestException> {
            documentService.uploadDocument(testUser.id!!, testOrder.id!!, mockFile, null)
        }

        assertEquals("Invalid file type. Allowed types: PDF, JPEG, PNG, GIF", exception.message)
    }

    @Test
    fun `getDocuments - returns documents for order`() {
        val documents = listOf(testDocument)

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder
        every { documentRepository.findByOrderId(testOrder.id!!) } returns documents

        val result = documentService.getDocuments(testUser.id!!, testOrder.id!!)

        assertEquals(1, result.size)
        assertEquals(testDocument.fileName, result[0].fileName)
        assertEquals(testDocument.originalFileName, result[0].originalFileName)
    }

    @Test
    fun `getDocument - returns single document`() {
        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder
        every { documentRepository.findByIdAndOrderId(testDocument.id!!, testOrder.id!!) } returns testDocument

        val result = documentService.getDocument(testUser.id!!, testOrder.id!!, testDocument.id!!)

        assertNotNull(result)
        assertEquals(testDocument.fileName, result.fileName)
        assertEquals(testDocument.originalFileName, result.originalFileName)
    }

    @Test
    fun `getDocument - throws when document not found`() {
        val documentId = UUID.randomUUID()

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder
        every { documentRepository.findByIdAndOrderId(documentId, testOrder.id!!) } returns null

        assertThrows<ResourceNotFoundException> {
            documentService.getDocument(testUser.id!!, testOrder.id!!, documentId)
        }
    }

    @Test
    fun `downloadDocument - returns resource and document`() {
        val resource = ByteArrayResource("test content".toByteArray())

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder
        every { documentRepository.findByIdAndOrderId(testDocument.id!!, testOrder.id!!) } returns testDocument
        every { storageService.load(testDocument.filePath) } returns resource

        val (resultResource, resultDocument) = documentService.downloadDocument(
            testUser.id!!, testOrder.id!!, testDocument.id!!
        )

        assertNotNull(resultResource)
        assertEquals(testDocument, resultDocument)
    }

    @Test
    fun `deleteDocument - deletes document successfully`() {
        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder
        every { documentRepository.findByIdAndOrderId(testDocument.id!!, testOrder.id!!) } returns testDocument
        every { storageService.delete(testDocument.filePath) } just runs
        every { documentRepository.delete(testDocument) } just runs

        documentService.deleteDocument(testUser.id!!, testOrder.id!!, testDocument.id!!)

        verify(exactly = 1) { storageService.delete(testDocument.filePath) }
        verify(exactly = 1) { documentRepository.delete(testDocument) }
    }

    @Test
    fun `deleteDocument - throws when order not in OPEN status`() {
        testOrder.status = OrderStatus.SUBMITTED

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder

        val exception = assertThrows<InvalidOperationException> {
            documentService.deleteDocument(testUser.id!!, testOrder.id!!, testDocument.id!!)
        }

        assertEquals("Documents can only be deleted from orders in OPEN status", exception.message)
    }

    @Test
    fun `deleteDocument - throws when document not found`() {
        val documentId = UUID.randomUUID()

        every { orderService.getOrderEntity(testUser.id!!, testOrder.id!!) } returns testOrder
        every { documentRepository.findByIdAndOrderId(documentId, testOrder.id!!) } returns null

        assertThrows<ResourceNotFoundException> {
            documentService.deleteDocument(testUser.id!!, testOrder.id!!, documentId)
        }
    }
}
