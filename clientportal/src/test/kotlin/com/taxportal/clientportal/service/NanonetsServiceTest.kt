package com.taxportal.clientportal.service

import com.taxportal.clientportal.exception.FileStorageException
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.ByteArrayResource
import org.springframework.web.reactive.function.client.WebClient
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NanonetsServiceTest {

    @MockK
    private lateinit var nanonetsWebClient: WebClient

    @MockK
    private lateinit var storageService: StorageService

    private lateinit var nanonetsService: NanonetsService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `submitDocument - returns null when model ID is not configured`() {
        nanonetsService = NanonetsService(
            nanonetsWebClient = nanonetsWebClient,
            storageService = storageService,
            modelId = "",
            webhookUrl = "http://localhost/webhook"
        )

        val result = nanonetsService.submitDocument(
            filePath = "/test/path.pdf",
            documentId = UUID.randomUUID(),
            orderId = UUID.randomUUID()
        )

        assertNull(result)
        verify(exactly = 0) { storageService.load(any()) }
    }

    @Test
    fun `submitDocument - throws when file cannot be loaded`() {
        nanonetsService = NanonetsService(
            nanonetsWebClient = nanonetsWebClient,
            storageService = storageService,
            modelId = "test-model-id",
            webhookUrl = "http://localhost/webhook"
        )

        val filePath = "/test/nonexistent.pdf"
        every { storageService.load(filePath) } throws FileStorageException("File not found")

        assertThrows<FileStorageException> {
            nanonetsService.submitDocument(
                filePath = filePath,
                documentId = UUID.randomUUID(),
                orderId = UUID.randomUUID()
            )
        }
    }

    @Test
    fun `parseExtractedFields - returns empty map for null predictions`() {
        nanonetsService = NanonetsService(
            nanonetsWebClient = nanonetsWebClient,
            storageService = storageService,
            modelId = "test-model-id",
            webhookUrl = ""
        )

        val result = nanonetsService.parseExtractedFields(null)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseExtractedFields - returns empty map for empty predictions`() {
        nanonetsService = NanonetsService(
            nanonetsWebClient = nanonetsWebClient,
            storageService = storageService,
            modelId = "test-model-id",
            webhookUrl = ""
        )

        val result = nanonetsService.parseExtractedFields(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseExtractedFields - extracts fields from predictions`() {
        nanonetsService = NanonetsService(
            nanonetsWebClient = nanonetsWebClient,
            storageService = storageService,
            modelId = "test-model-id",
            webhookUrl = ""
        )

        val predictions = listOf(
            mapOf("label" to "employer_name", "ocr_text" to "ACME Corp"),
            mapOf("label" to "income", "ocr_text" to "50000.00"),
            mapOf("label" to "tax_year", "ocr_text" to "2024")
        )

        val result = nanonetsService.parseExtractedFields(predictions)

        assertEquals(3, result.size)
        assertEquals("ACME Corp", result["employer_name"])
        assertEquals("50000.00", result["income"])
        assertEquals("2024", result["tax_year"])
    }

    @Test
    fun `parseExtractedFields - trims whitespace from ocr_text`() {
        nanonetsService = NanonetsService(
            nanonetsWebClient = nanonetsWebClient,
            storageService = storageService,
            modelId = "test-model-id",
            webhookUrl = ""
        )

        val predictions = listOf(
            mapOf("label" to "field_name", "ocr_text" to "  value with spaces  ")
        )

        val result = nanonetsService.parseExtractedFields(predictions)

        assertEquals("value with spaces", result["field_name"])
    }

    @Test
    fun `parseExtractedFields - skips predictions without label`() {
        nanonetsService = NanonetsService(
            nanonetsWebClient = nanonetsWebClient,
            storageService = storageService,
            modelId = "test-model-id",
            webhookUrl = ""
        )

        val predictions = listOf(
            mapOf("ocr_text" to "value without label"),
            mapOf("label" to "valid_field", "ocr_text" to "valid value")
        )

        val result = nanonetsService.parseExtractedFields(predictions)

        assertEquals(1, result.size)
        assertEquals("valid value", result["valid_field"])
    }

    @Test
    fun `parseExtractedFields - handles missing ocr_text`() {
        nanonetsService = NanonetsService(
            nanonetsWebClient = nanonetsWebClient,
            storageService = storageService,
            modelId = "test-model-id",
            webhookUrl = ""
        )

        val predictions = listOf(
            mapOf("label" to "field_without_text")
        )

        val result = nanonetsService.parseExtractedFields(predictions)

        assertEquals(1, result.size)
        assertEquals("", result["field_without_text"])
    }
}
