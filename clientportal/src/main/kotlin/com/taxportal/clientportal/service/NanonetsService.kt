package com.taxportal.clientportal.service

import com.taxportal.clientportal.dto.NanonetsSubmitResponse
import com.taxportal.clientportal.exception.FileStorageException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.*

@Service
class NanonetsService(
    private val nanonetsWebClient: WebClient,
    private val storageService: StorageService,
    @Value("\${nanonets.model-id:}")
    private val modelId: String,
    @Value("\${nanonets.webhook-url:}")
    private val webhookUrl: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun submitDocument(
        filePath: String,
        documentId: UUID,
        orderId: UUID
    ): NanonetsSubmitResponse? {
        if (modelId.isBlank()) {
            logger.warn("Nanonets model ID not configured, skipping extraction")
            return null
        }

        val resource: Resource
        try {
            resource = storageService.load(filePath)
        } catch (e: FileStorageException) {
            logger.error("Failed to load file for extraction: $filePath", e)
            throw e
        }

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", resource)
            .filename(resource.filename ?: "document.pdf")
            .contentType(MediaType.APPLICATION_PDF)

        // Add metadata for webhook callback
        val metadata = mapOf(
            "document_id" to documentId.toString(),
            "order_id" to orderId.toString()
        )
        bodyBuilder.part("request_metadata", metadata.toString())

        // Add webhook URL if configured
        if (webhookUrl.isNotBlank()) {
            bodyBuilder.part("async", "true")
            bodyBuilder.part("webhook_url", webhookUrl)
        }

        return try {
            val response = nanonetsWebClient.post()
                .uri("/OCR/Model/$modelId/LabelFile")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(NanonetsSubmitResponse::class.java)
                .block()

            logger.info("Document submitted to Nanonets successfully: $documentId")
            response
        } catch (e: WebClientResponseException) {
            logger.error("Nanonets API error: ${e.statusCode} - ${e.responseBodyAsString}", e)
            throw RuntimeException("Nanonets API error: ${e.message}", e)
        } catch (e: Exception) {
            logger.error("Failed to submit document to Nanonets: $documentId", e)
            throw RuntimeException("Failed to submit document for extraction: ${e.message}", e)
        }
    }

    fun parseExtractedFields(predictions: List<Map<String, Any>>?): Map<String, String> {
        if (predictions.isNullOrEmpty()) return emptyMap()

        val fields = mutableMapOf<String, String>()

        for (prediction in predictions) {
            val label = prediction["label"] as? String ?: continue
            val ocrText = prediction["ocr_text"] as? String ?: ""
            fields[label] = ocrText.trim()
        }

        return fields
    }
}
