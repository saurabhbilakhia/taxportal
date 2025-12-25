package com.taxportal.clientportal.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "documents")
class Document(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column(name = "file_name", nullable = false)
    val fileName: String,

    @Column(name = "original_file_name", nullable = false)
    val originalFileName: String,

    @Column(name = "file_path", nullable = false)
    val filePath: String,

    @Column(name = "file_size")
    val fileSize: Long? = null,

    @Column(name = "mime_type")
    val mimeType: String? = null,

    @Column(name = "slip_type")
    val slipType: String? = null,

    @Column(name = "uploaded_at")
    val uploadedAt: Instant = Instant.now()
)
