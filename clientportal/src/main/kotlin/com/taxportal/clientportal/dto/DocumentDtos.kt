package com.taxportal.clientportal.dto

import com.taxportal.clientportal.entity.Document
import java.time.Instant
import java.util.*

data class DocumentResponse(
    val id: UUID,
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long?,
    val mimeType: String?,
    val slipType: String?,
    val uploadedAt: Instant
) {
    companion object {
        fun from(document: Document): DocumentResponse {
            return DocumentResponse(
                id = document.id!!,
                fileName = document.fileName,
                originalFileName = document.originalFileName,
                fileSize = document.fileSize,
                mimeType = document.mimeType,
                slipType = document.slipType,
                uploadedAt = document.uploadedAt
            )
        }
    }
}

data class DocumentUploadResponse(
    val id: UUID,
    val fileName: String,
    val slipType: String?,
    val uploadedAt: Instant
)
