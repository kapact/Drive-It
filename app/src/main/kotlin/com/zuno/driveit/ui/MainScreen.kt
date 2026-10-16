package com.zuno.driveit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zuno.driveit.service.GoogleDriveService
import com.zuno.driveit.util.CompressionUtil
import com.zuno.driveit.util.PreferencesManager
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onShowMessage: (String) -> Unit) {
    var selectedFolder by remember { mutableStateOf<File?>(null) }
    var folderId by remember { mutableStateOf(PreferencesManager.getFolderId()) }
    var isProcessing by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf("") }
    var progressValue by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Load last selected folder on startup
    LaunchedEffect(Unit) {
        val lastFolder = PreferencesManager.getLastSelectedFolder()
        if (lastFolder.isNotBlank()) {
            val file = File(lastFolder)
            if (file.exists() && file.isDirectory) {
                selectedFolder = file
            }
        }
    }

    // Save folder ID whenever it changes
    LaunchedEffect(folderId) {
        PreferencesManager.saveFolderId(folderId)
    }

    // Custom gradient colors
    val primaryGradient = listOf(
        androidx.compose.ui.graphics.Color(0xFF6366F1), // Indigo
        androidx.compose.ui.graphics.Color(0xFF8B5CF6)  // Purple
    )

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Header with gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(primaryGradient)
                    )
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DriveIt",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        text = "Compress & Upload to Google Drive",
                        style = MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                    )

                    if (!PreferencesManager.isConfigured()) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = "⚠️ App not properly configured. Please contact developer.",
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Main Upload Button (BIG CTA) - NOW AT TOP!
            Button(
                onClick = {
                    scope.launch {
                        val folder = selectedFolder
                        if (folder == null) {
                            onShowMessage("Please select a folder first")
                            return@launch
                        }

                        isProcessing = true

                        try {
                            // Step 1: Compress
                            progressMessage = "Compressing folder..."
                            progressValue = 0f

                            val compressionResult = CompressionUtil.compressFolder(folder) { progress, message ->
                                progressValue = progress * 0.5f
                                progressMessage = message
                            }

                            if (compressionResult.isFailure) {
                                onShowMessage("Compression failed: ${compressionResult.exceptionOrNull()?.message}")
                                return@launch
                            }

                            val zipFile = compressionResult.getOrNull()!!

                            // Step 2: Initialize Drive Service
                            progressMessage = "Authorizing with Google Drive... (check your browser if prompted)"
                            progressValue = 0.5f

                            val driveService = GoogleDriveService(PreferencesManager.getCredentialsPath())
                            val initResult = driveService.initialize()

                            if (initResult.isFailure) {
                                zipFile.delete()
                                val errorMsg = initResult.exceptionOrNull()?.message ?: "Unknown error"
                                onShowMessage("Authorization failed: $errorMsg")
                                return@launch
                            }

                            // Step 3: Upload
                            progressMessage = "Uploading to Google Drive..."
                            progressValue = 0.7f

                            val targetFolderId = folderId.takeIf { it.isNotBlank() }
                            val uploadResult = driveService.uploadFile(
                                zipFile,
                                targetFolderId
                            ) { progress ->
                                progressValue = 0.7f + (progress * 0.3f)
                            }

                            // Cleanup
                            zipFile.delete()

                            if (uploadResult.isSuccess) {
                                progressValue = 1f
                                progressMessage = "Upload complete!"
                                onShowMessage("Successfully uploaded to Google Drive!")
                            } else {
                                onShowMessage("Upload failed: ${uploadResult.exceptionOrNull()?.message}")
                            }

                        } catch (e: Exception) {
                            onShowMessage("Error: ${e.message}")
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                enabled = !isProcessing && selectedFolder != null && PreferencesManager.isConfigured(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF6366F1),
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    disabledContainerColor = androidx.compose.ui.graphics.Color(0xFFE2E8F0),
                    disabledContentColor = androidx.compose.ui.graphics.Color(0xFF94A3B8)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Upload to Google Drive",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Folder Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = androidx.compose.ui.graphics.Color(0xFF6366F1).copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = androidx.compose.ui.graphics.Color(0xFF6366F1)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Selected Folder",
                                style = MaterialTheme.typography.labelMedium,
                                color = androidx.compose.ui.graphics.Color(0xFF64748B)
                            )
                            Text(
                                text = selectedFolder?.name ?: "No folder selected",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color(0xFF1E293B)
                            )
                            selectedFolder?.let { folder ->
                                val fileCount = folder.walkTopDown().count { it.isFile }
                                val totalSize = folder.walkTopDown()
                                    .filter { it.isFile }
                                    .sumOf { it.length() }
                                Text(
                                    text = "$fileCount files • ${CompressionUtil.formatFileSize(totalSize)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = androidx.compose.ui.graphics.Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val fileChooser = JFileChooser().apply {
                                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                dialogTitle = "Select Folder to Upload"
                                isMultiSelectionEnabled = false

                                // Start from last selected folder's parent if available
                                selectedFolder?.parentFile?.let {
                                    currentDirectory = it
                                }
                            }

                            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                selectedFolder = fileChooser.selectedFile
                                PreferencesManager.saveLastSelectedFolder(fileChooser.selectedFile.absolutePath)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isProcessing,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            androidx.compose.ui.graphics.Color(0xFF6366F1)
                        )
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFF6366F1)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (selectedFolder == null) "Select Folder" else "Change Folder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = androidx.compose.ui.graphics.Color(0xFF6366F1)
                        )
                    }
                }
            }

            // Folder ID Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = androidx.compose.ui.graphics.Color(0xFF10B981)
                            )
                        }
                        Column {
                            Text(
                                text = "Google Drive Destination",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color(0xFF1E293B)
                            )
                            Text(
                                text = "Optional folder location",
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color(0xFF64748B)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = folderId,
                        onValueChange = { folderId = it },
                        label = { Text("Folder ID") },
                        placeholder = { Text("Leave empty for root folder") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF6366F1),
                            focusedLabelColor = androidx.compose.ui.graphics.Color(0xFF6366F1)
                        ),
                        supportingText = {
                            Text(
                                "Find ID in Google Drive URL after /folders/",
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color(0xFF64748B)
                            )
                        }
                    )
                }
            }

            // Progress Section
            AnimatedVisibility(visible = isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFFEF3C7)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 4.dp,
                                color = androidx.compose.ui.graphics.Color(0xFFF59E0B)
                            )
                            Text(
                                text = progressMessage,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = androidx.compose.ui.graphics.Color(0xFF92400E)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                            trackColor = androidx.compose.ui.graphics.Color(0xFFFDE68A),
                        )
                    }
                }
            }
            }
        }
    }
}
