package com.zuno.driveit.util

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.prefs.Preferences

object PreferencesManager {
    private val prefs = Preferences.userNodeForPackage(PreferencesManager::class.java)

    private const val KEY_FOLDER_ID = "folder_id"
    private const val KEY_LAST_SELECTED_FOLDER = "last_selected_folder"

    // Token storage directory (where OAuth tokens are saved after user authorization)
    private val TOKEN_STORAGE_DIR = File(System.getProperty("user.home"), ".driveit")

    // Get credentials - first try to extract from bundled resources
    fun getCredentialsPath(): String {
        // Try to load credentials from bundled resources
        val resourceStream: InputStream? = PreferencesManager::class.java.classLoader?.getResourceAsStream("credentials.json")

        if (resourceStream != null) {
            // Extract bundled credentials to temp location for Google API to read
            val tempCredsFile = File(TOKEN_STORAGE_DIR, "credentials.json")

            // Create directory if needed
            if (!TOKEN_STORAGE_DIR.exists()) {
                TOKEN_STORAGE_DIR.mkdirs()
            }

            // Write resource to file if it doesn't exist or is different
            if (!tempCredsFile.exists()) {
                FileOutputStream(tempCredsFile).use { output ->
                    resourceStream.copyTo(output)
                }
            }

            resourceStream.close()
            return tempCredsFile.absolutePath
        }

        // Fallback: check development location
        var current = File(System.getProperty("user.dir"))
        while (current.parentFile != null && !File(current, "build.gradle.kts").exists()) {
            current = current.parentFile ?: break
        }

        val projectCredentials = File(current, "app/credentials/credentials.json")
        if (projectCredentials.exists()) {
            return projectCredentials.absolutePath
        }

        return ""
    }

    fun saveFolderId(folderId: String) {
        prefs.put(KEY_FOLDER_ID, folderId)
        prefs.flush()
    }

    fun getFolderId(): String {
        return prefs.get(KEY_FOLDER_ID, "")
    }

    fun saveLastSelectedFolder(folderPath: String) {
        prefs.put(KEY_LAST_SELECTED_FOLDER, folderPath)
        prefs.flush()
    }

    fun getLastSelectedFolder(): String {
        return prefs.get(KEY_LAST_SELECTED_FOLDER, "")
    }

    fun isConfigured(): Boolean {
        val credPath = getCredentialsPath()
        return credPath.isNotBlank()
    }

    fun getCredentialsDirectory(): File {
        return TOKEN_STORAGE_DIR
    }

    fun getTokenStoragePath(): File {
        if (!TOKEN_STORAGE_DIR.exists()) {
            TOKEN_STORAGE_DIR.mkdirs()
        }
        return TOKEN_STORAGE_DIR
    }
}
