package com.zuno.driveit

import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(507.dp, 844.dp),  // Mobile form factor with 30% wider width
        position = WindowPosition.Aligned(Alignment.Center)  // Center on screen
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "DriveIt - Upload to Google Drive",
        state = windowState
    ) {
        DriveItApp()
    }
}
