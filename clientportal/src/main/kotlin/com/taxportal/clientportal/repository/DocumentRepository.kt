package com.taxportal.clientportal.repository

import com.taxportal.clientportal.entity.Document
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DocumentRepository : JpaRepository<Document, UUID> {
    fun findByOrderId(orderId: UUID): List<Document>

    fun findByIdAndOrderId(id: UUID, orderId: UUID): Document?

    fun deleteByIdAndOrderId(id: UUID, orderId: UUID): Int
}
