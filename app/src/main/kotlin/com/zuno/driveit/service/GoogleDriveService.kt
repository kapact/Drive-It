package com.zuno.driveit.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.zuno.driveit.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class GoogleDriveService(
    private val credentialsPath: String
) {
    private var driveService: Drive? = null
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val scopes = listOf(DriveScopes.DRIVE_FILE)

    // Store tokens in user's home directory (~/.driveit)
    private val tokenStorePath = PreferencesManager.getTokenStoragePath()

    private fun getCredentials(): Credential {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

        // Load client secrets
        val clientSecrets = GoogleClientSecrets.load(
            jsonFactory,
            InputStreamReader(FileInputStream(credentialsPath))
        )

        // Build flow and trigger user authorization request
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport,
            jsonFactory,
            clientSecrets,
            scopes
        )
            .setDataStoreFactory(FileDataStoreFactory(tokenStorePath))
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder()
            .setPort(8888)
            .build()

        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Set timeout for the entire initialization (2 minutes)
            withTimeout(120000) {
                val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                val credential = getCredentials()

                // Test the credential by making a simple API call
                val testService = Drive.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName("DriveIt")
                    .build()

                // Verify we can actually access Drive
                testService.about().get().setFields("user").execute()

                driveService = testService
                Result.success(Unit)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Authorization timeout. Please try again and complete the authorization in the browser."))
        } catch (e: Exception) {
            Result.failure(Exception("Authorization failed: ${e.message}. Try clearing credentials: rm -rf ~/.driveit"))
        }
    }

    suspend fun uploadFile(
        file: File,
        folderId: String? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(
                IllegalStateException("Drive service not initialized. Please authorize first.")
            )

            // Search for existing file with same name in the target folder
            val query = if (folderId != null && folderId.isNotBlank()) {
                "name='${file.name}' and '${folderId}' in parents and trashed=false"
            } else {
                "name='${file.name}' and 'root' in parents and trashed=false"
            }

            val existingFiles = service.files()
                .list()
                .setQ(query)
                .setFields("files(id, name)")
                .execute()
                .files

            // Delete existing files with the same name
            existingFiles.forEach { existingFile ->
                service.files().delete(existingFile.id).execute()
            }

            onProgress(0.5f)

            // Upload the new file
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = file.name
                if (folderId != null && folderId.isNotBlank()) {
                    parents = listOf(folderId)
                }
            }

            val mediaContent = FileContent("application/zip", file)

            val uploadedFile = service.files()
                .create(fileMetadata, mediaContent)
                .setFields("id, name, webViewLink")
                .execute()

            onProgress(1f)

            Result.success(uploadedFile.webViewLink ?: uploadedFile.id)
        } catch (e: Exception) {
            Result.failure(Exception("Upload failed: ${e.message}"))
        }
    }

    suspend fun verifyFolderAccess(folderId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(
                IllegalStateException("Drive service not initialized")
            )

            val folder = service.files()
                .get(folderId)
                .setFields("id, name")
                .execute()

            Result.success(folder.name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun clearStoredCredentials() {
            val tokenStorePath = PreferencesManager.getTokenStoragePath()
            val storedCredFile = File(tokenStorePath, "StoredCredential")
            if (storedCredFile.exists()) {
                storedCredFile.delete()
            }
        }
    }
}
