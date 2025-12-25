package com.taxportal.clientportal.service

import com.taxportal.clientportal.exception.FileStorageException
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class StorageService(
    @Value("\${app.upload.dir:uploads}")
    private val uploadDir: String
) {
    private lateinit var rootLocation: Path

    @PostConstruct
    fun init() {
        rootLocation = Paths.get(uploadDir)
        try {
            Files.createDirectories(rootLocation)
        } catch (e: Exception) {
            throw FileStorageException("Could not initialize storage location", e)
        }
    }

    fun store(file: MultipartFile, orderId: UUID): String {
        if (file.isEmpty) {
            throw FileStorageException("Failed to store empty file")
        }

        val originalFilename = file.originalFilename ?: "unknown"
        val extension = originalFilename.substringAfterLast(".", "")
        val storedFileName = "${UUID.randomUUID()}.$extension"

        val orderDir = rootLocation.resolve(orderId.toString())
        try {
            Files.createDirectories(orderDir)
            val destinationFile = orderDir.resolve(storedFileName)
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: Exception) {
            throw FileStorageException("Failed to store file $originalFilename", e)
        }

        return "$orderId/$storedFileName"
    }

    fun load(filePath: String): Resource {
        val file = rootLocation.resolve(filePath)
        val resource = UrlResource(file.toUri())
        if (resource.exists() && resource.isReadable) {
            return resource
        } else {
            throw FileStorageException("Could not read file: $filePath")
        }
    }

    fun delete(filePath: String) {
        try {
            val file = rootLocation.resolve(filePath)
            Files.deleteIfExists(file)
        } catch (e: Exception) {
            throw FileStorageException("Could not delete file: $filePath", e)
        }
    }

    fun deleteOrderDirectory(orderId: UUID) {
        try {
            val orderDir = rootLocation.resolve(orderId.toString())
            if (Files.exists(orderDir)) {
                Files.walk(orderDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach { Files.deleteIfExists(it) }
            }
        } catch (e: Exception) {
            throw FileStorageException("Could not delete order directory: $orderId", e)
        }
    }
}
