package com.taxportal.clientportal.service

import com.taxportal.clientportal.dto.DocumentResponse
import com.taxportal.clientportal.dto.DocumentUploadResponse
import com.taxportal.clientportal.entity.Document
import com.taxportal.clientportal.entity.OrderStatus
import com.taxportal.clientportal.exception.BadRequestException
import com.taxportal.clientportal.exception.InvalidOperationException
import com.taxportal.clientportal.exception.ResourceNotFoundException
import com.taxportal.clientportal.repository.DocumentRepository
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val orderService: OrderService,
    private val storageService: StorageService
) {
    companion object {
        private val ALLOWED_CONTENT_TYPES = setOf(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/gif"
        )
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
    }

    @Transactional
    fun uploadDocument(
        userId: UUID,
        orderId: UUID,
        file: MultipartFile,
        slipType: String?
    ): DocumentUploadResponse {
        val order = orderService.getOrderEntity(userId, orderId)

        if (order.status != OrderStatus.OPEN) {
            throw InvalidOperationException("Documents can only be uploaded to orders in OPEN status")
        }

        validateFile(file)

        val filePath = storageService.store(file, orderId)
        val storedFileName = filePath.substringAfterLast("/")

        val document = Document(
            order = order,
            fileName = storedFileName,
            originalFileName = file.originalFilename ?: "unknown",
            filePath = filePath,
            fileSize = file.size,
            mimeType = file.contentType,
            slipType = slipType
        )

        val savedDocument = documentRepository.save(document)

        return DocumentUploadResponse(
            id = savedDocument.id!!,
            fileName = savedDocument.originalFileName,
            slipType = savedDocument.slipType,
            uploadedAt = savedDocument.uploadedAt
        )
    }

    fun getDocuments(userId: UUID, orderId: UUID): List<DocumentResponse> {
        orderService.getOrderEntity(userId, orderId)
        return documentRepository.findByOrderId(orderId).map { DocumentResponse.from(it) }
    }

    fun getDocument(userId: UUID, orderId: UUID, documentId: UUID): DocumentResponse {
        orderService.getOrderEntity(userId, orderId)
        val document = documentRepository.findByIdAndOrderId(documentId, orderId)
            ?: throw ResourceNotFoundException("Document not found")
        return DocumentResponse.from(document)
    }

    fun downloadDocument(userId: UUID, orderId: UUID, documentId: UUID): Pair<Resource, Document> {
        orderService.getOrderEntity(userId, orderId)
        val document = documentRepository.findByIdAndOrderId(documentId, orderId)
            ?: throw ResourceNotFoundException("Document not found")
        val resource = storageService.load(document.filePath)
        return Pair(resource, document)
    }

    @Transactional
    fun deleteDocument(userId: UUID, orderId: UUID, documentId: UUID) {
        val order = orderService.getOrderEntity(userId, orderId)

        if (order.status != OrderStatus.OPEN) {
            throw InvalidOperationException("Documents can only be deleted from orders in OPEN status")
        }

        val document = documentRepository.findByIdAndOrderId(documentId, orderId)
            ?: throw ResourceNotFoundException("Document not found")

        storageService.delete(document.filePath)
        documentRepository.delete(document)
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw BadRequestException("File is empty")
        }

        if (file.size > MAX_FILE_SIZE) {
            throw BadRequestException("File size exceeds maximum allowed size of 10MB")
        }

        val contentType = file.contentType
        if (contentType == null || contentType !in ALLOWED_CONTENT_TYPES) {
            throw BadRequestException("Invalid file type. Allowed types: PDF, JPEG, PNG, GIF")
        }
    }
}
