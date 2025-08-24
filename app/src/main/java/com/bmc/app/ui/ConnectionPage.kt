package com.bmc.app.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bmc.app.ui.components.QrScanner
import kotlinx.coroutines.launch

@SuppressLint("ContextCastToActivity")
@Composable
fun ConnectionPage(
    onExit: () -> Unit,
    onAddressSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val requestPermissionLauncher = rememberLauncherForActivityResult (
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is needed to scan QR codes")
            }
        }
    }

    var addressInput by remember { mutableStateOf("") }

    Scaffold (
        topBar = {
            TopBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                onExit = onExit
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { padding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text("Scan addon QR code")
            Box (
                modifier = Modifier
                    .size(width = 400.dp, height = 500.dp)
            ) {
                if (ContextCompat.checkSelfPermission(
                        LocalContext.current,
                        android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    QrScanner(onQrCodeScanned = { onAddressSubmit(it) })
                } else {
//                            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                                    LocalContext.current as android.app.Activity,
//                                    android.Manifest.permission.CAMERA
//                                )
//                            ) {
//                                Dialog({}) {
//                                    Surface {
//                                        Text("Camera permission is needed to scan QR codes. Alternatively, the host IP address can be entered manually.")
//                                    }
//                                }
//                            }
                    Text(
                        "Camera permission not granted",
                        modifier = Modifier.align(Alignment.Center)
                    )
                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }
            Text("Or enter address manually")
            TextField(
                value = addressInput,
                onValueChange = { addressInput = it },
                placeholder = { Text("IP Address : Port") },
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onAddressSubmit(addressInput) }
                ),
                singleLine = true
            )
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier,
    onExit: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Connect to addon",
            modifier = Modifier.align(Alignment.Center)
        )
        Button(
            onClick = onExit,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close"
            )
        }
    }
}
