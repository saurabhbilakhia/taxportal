package com.taxportal.clientportal.repository

import com.taxportal.clientportal.entity.ExtractionOverride
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExtractionOverrideRepository : JpaRepository<ExtractionOverride, UUID> {

    @Query("SELECT o FROM ExtractionOverride o WHERE o.extractionResult.id = :extractionResultId ORDER BY o.createdAt DESC")
    fun findByExtractionResultIdOrderByCreatedAtDesc(extractionResultId: UUID): List<ExtractionOverride>

    fun countByExtractionResultId(extractionResultId: UUID): Long

    @Query("SELECT o FROM ExtractionOverride o WHERE o.overriddenBy.id = :userId ORDER BY o.createdAt DESC")
    fun findByOverriddenByIdOrderByCreatedAtDesc(userId: UUID): List<ExtractionOverride>
}
