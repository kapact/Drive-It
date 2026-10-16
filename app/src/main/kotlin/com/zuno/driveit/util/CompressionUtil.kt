package com.zuno.driveit.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CompressionUtil {

    suspend fun compressFolder(
        sourceFolder: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!sourceFolder.exists() || !sourceFolder.isDirectory) {
                return@withContext Result.failure(
                    IllegalArgumentException("Source must be a valid directory")
                )
            }

            // Use folder name for the ZIP file (in temp directory)
            val outputFile = File(
                System.getProperty("java.io.tmpdir"),
                "${sourceFolder.name}.zip"
            )

            val allFiles = sourceFolder.walkTopDown()
                .filter { it.isFile }
                .toList()

            val totalFiles = allFiles.size
            var processedFiles = 0

            ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
                allFiles.forEach { file ->
                    val relativePath = sourceFolder.toPath().relativize(file.toPath()).toString()
                    onProgress(
                        processedFiles.toFloat() / totalFiles,
                        "Compressing: $relativePath"
                    )

                    val zipEntry = ZipEntry(relativePath)
                    zipOut.putNextEntry(zipEntry)

                    FileInputStream(file).use { input ->
                        input.copyTo(zipOut)
                    }

                    zipOut.closeEntry()
                    processedFiles++
                }
            }

            onProgress(1f, "Compression complete")
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$bytes bytes"
        }
    }
}
