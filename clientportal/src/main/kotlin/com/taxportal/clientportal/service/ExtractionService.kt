package com.taxportal.clientportal.service

import com.taxportal.clientportal.dto.ExtractionResultResponse
import com.taxportal.clientportal.dto.NanonetsWebhookPayload
import com.taxportal.clientportal.dto.OrderExtractionResponse
import com.taxportal.clientportal.entity.*
import com.taxportal.clientportal.exception.ResourceNotFoundException
import com.taxportal.clientportal.repository.DocumentRepository
import com.taxportal.clientportal.repository.ExtractionResultRepository
import com.taxportal.clientportal.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class ExtractionService(
    private val extractionResultRepository: ExtractionResultRepository,
    private val orderRepository: OrderRepository,
    private val documentRepository: DocumentRepository,
    private val nanonetsService: NanonetsService,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async("extractionExecutor")
    @Transactional
    fun triggerExtractionForOrder(orderId: UUID) {
        logger.info("Starting extraction workflow for order: $orderId")

        val order = orderRepository.findById(orderId)
            .orElseThrow { ResourceNotFoundException("Order not found: $orderId") }

        val documents = documentRepository.findByOrderId(orderId)

        if (documents.isEmpty()) {
            logger.warn("No documents found for order: $orderId")
            return
        }

        for (document in documents) {
            try {
                submitDocumentForExtraction(order, document)
            } catch (e: Exception) {
                logger.error("Failed to submit document ${document.id} for extraction", e)
                createFailedExtractionResult(order, document, e.message)
            }
        }
    }

    @Transactional
    fun submitDocumentForExtraction(order: Order, document: Document): ExtractionResult {
        // Check if extraction already exists
        val existing = extractionResultRepository.findByDocumentId(document.id!!)
        if (existing != null) {
            logger.info("Extraction already exists for document: ${document.id}")
            return existing
        }

        // Create extraction result record
        val extractionResult = ExtractionResult(
            document = document,
            order = order,
            status = ExtractionStatus.PROCESSING
        )
        val savedResult = extractionResultRepository.save(extractionResult)

        // Submit to Nanonets
        try {
            val response = nanonetsService.submitDocument(
                filePath = document.filePath,
                documentId = document.id!!,
                orderId = order.id!!
            )

            if (response?.result?.isNotEmpty() == true) {
                val firstResult = response.result.first()
                savedResult.nanonetsRequestId = firstResult.requestFileId

                // If sync response with predictions, process immediately
                if (firstResult.prediction?.isNotEmpty() == true) {
                    processExtractionResult(savedResult, response)
                }
            }

            extractionResultRepository.save(savedResult)
            logger.info("Document submitted for extraction: ${document.id}")

        } catch (e: Exception) {
            savedResult.status = ExtractionStatus.FAILED
            savedResult.errorMessage = e.message
            extractionResultRepository.save(savedResult)
            throw e
        }

        return savedResult
    }

    @Transactional
    fun handleWebhookCallback(payload: NanonetsWebhookPayload) {
        val requestFileId = payload.requestFileId
            ?: payload.result?.firstOrNull()?.requestFileId

        if (requestFileId == null) {
            logger.warn("Webhook received without request file ID")
            return
        }

        val extractionResult = extractionResultRepository.findByNanonetsRequestId(requestFileId)
        if (extractionResult == null) {
            // Try to find from metadata
            val metadata = payload.requestMetadata
            if (metadata != null) {
                logger.info("Looking for extraction by metadata: $metadata")
            }
            logger.warn("No extraction result found for request ID: $requestFileId")
            return
        }

        processWebhookPayload(extractionResult, payload)
    }

    @Transactional
    fun processWebhookPayload(extractionResult: ExtractionResult, payload: NanonetsWebhookPayload) {
        try {
            val predictions = payload.result?.flatMap { result ->
                result.prediction?.map { prediction ->
                    mapOf(
                        "label" to (prediction.label ?: ""),
                        "ocr_text" to (prediction.ocrText ?: ""),
                        "score" to (prediction.score ?: 0.0)
                    )
                } ?: emptyList()
            } ?: emptyList()

            val extractedFields = nanonetsService.parseExtractedFields(predictions)

            extractionResult.extractedData = mapOf(
                "fields" to extractedFields,
                "slip_type" to (extractionResult.document.slipType ?: "unknown")
            )
            extractionResult.rawResponse = mapOf(
                "message" to (payload.message ?: ""),
                "result_count" to (payload.result?.size ?: 0)
            )
            extractionResult.status = ExtractionStatus.COMPLETED
            extractionResult.completedAt = Instant.now()

            extractionResultRepository.save(extractionResult)
            logger.info("Extraction completed for document: ${extractionResult.document.id}")

            // Check if all documents for this order are processed
            checkOrderExtractionCompletion(extractionResult.order.id!!)

        } catch (e: Exception) {
            logger.error("Failed to process webhook payload for ${extractionResult.id}", e)
            extractionResult.status = ExtractionStatus.FAILED
            extractionResult.errorMessage = e.message
            extractionResultRepository.save(extractionResult)
        }
    }

    private fun processExtractionResult(
        extractionResult: ExtractionResult,
        response: com.taxportal.clientportal.dto.NanonetsSubmitResponse
    ) {
        val predictions = response.result?.flatMap { result ->
            result.prediction?.map { prediction ->
                mapOf(
                    "label" to (prediction.label ?: ""),
                    "ocr_text" to (prediction.ocrText ?: ""),
                    "score" to (prediction.score ?: 0.0)
                )
            } ?: emptyList()
        } ?: emptyList()

        val extractedFields = nanonetsService.parseExtractedFields(predictions)

        extractionResult.extractedData = mapOf(
            "fields" to extractedFields,
            "slip_type" to (extractionResult.document.slipType ?: "unknown")
        )
        extractionResult.status = ExtractionStatus.COMPLETED
        extractionResult.completedAt = Instant.now()
    }

    private fun createFailedExtractionResult(order: Order, document: Document, errorMessage: String?) {
        val extractionResult = ExtractionResult(
            document = document,
            order = order,
            status = ExtractionStatus.FAILED,
            errorMessage = errorMessage
        )
        extractionResultRepository.save(extractionResult)
    }

    @Transactional
    fun checkOrderExtractionCompletion(orderId: UUID) {
        val totalDocuments = extractionResultRepository.countByOrderId(orderId)
        val completedCount = extractionResultRepository.countByOrderIdAndStatus(orderId, ExtractionStatus.COMPLETED)
        val failedCount = extractionResultRepository.countByOrderIdAndStatus(orderId, ExtractionStatus.FAILED)

        val processedCount = completedCount + failedCount

        if (processedCount >= totalDocuments) {
            logger.info("All extractions completed for order: $orderId (completed: $completedCount, failed: $failedCount)")

            // Update order status to IN_REVIEW
            val order = orderRepository.findById(orderId).orElse(null)
            if (order != null && order.status == OrderStatus.SUBMITTED) {
                order.status = OrderStatus.IN_REVIEW
                orderRepository.save(order)

                // Send notification to accountant
                notificationService.sendReviewReadyNotification(order)
            }
        }
    }

    fun getOrderExtractions(userId: UUID, orderId: UUID): OrderExtractionResponse {
        return getOrderExtractionsForAccountant(orderId)
    }

    fun getOrderExtractionsForAccountant(orderId: UUID): OrderExtractionResponse {
        val results = extractionResultRepository.findByOrderId(orderId)

        val completedCount = results.count { it.status == ExtractionStatus.COMPLETED }
        val pendingCount = results.count { it.status == ExtractionStatus.PENDING || it.status == ExtractionStatus.PROCESSING }
        val failedCount = results.count { it.status == ExtractionStatus.FAILED }

        return OrderExtractionResponse(
            orderId = orderId,
            totalDocuments = results.size,
            completedExtractions = completedCount,
            pendingExtractions = pendingCount,
            failedExtractions = failedCount,
            results = results.map { ExtractionResultResponse.from(it) }
        )
    }

    @Transactional
    fun retryFailedExtraction(orderId: UUID, documentId: UUID): ExtractionResultResponse {
        val extractionResult = extractionResultRepository.findByDocumentId(documentId)
            ?: throw ResourceNotFoundException("Extraction result not found for document: $documentId")

        if (extractionResult.order.id != orderId) {
            throw ResourceNotFoundException("Document does not belong to the specified order")
        }

        if (extractionResult.status != ExtractionStatus.FAILED) {
            throw IllegalStateException("Can only retry failed extractions")
        }

        // Reset and resubmit
        extractionResult.status = ExtractionStatus.PROCESSING
        extractionResult.errorMessage = null
        extractionResult.extractedData = null
        extractionResultRepository.save(extractionResult)

        try {
            nanonetsService.submitDocument(
                filePath = extractionResult.document.filePath,
                documentId = documentId,
                orderId = orderId
            )
        } catch (e: Exception) {
            extractionResult.status = ExtractionStatus.FAILED
            extractionResult.errorMessage = e.message
            extractionResultRepository.save(extractionResult)
        }

        return ExtractionResultResponse.from(extractionResult)
    }
}
