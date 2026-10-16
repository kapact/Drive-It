package com.zuno.driveit

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.zuno.driveit.ui.MainScreen
import kotlinx.coroutines.launch

@Composable
fun DriveItApp() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    MaterialTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            MainScreen(
                onShowMessage = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )
        }
    }
}
