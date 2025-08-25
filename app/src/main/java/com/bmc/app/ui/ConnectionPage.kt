package com.bmc.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bmc.app.ui.components.QrScanner
import com.bmc.app.ui.components.TopBar
import com.bmc.app.ui.components.TopBarButton
import com.bmc.app.ui.theme.Dimens
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
    val context = LocalContext.current

    var addressInput by remember { mutableStateOf("") }
    var askedForPermission by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult (
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        hasCameraPermission = granted
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is needed to scan QR codes")
            }
        }
    }

    Scaffold (
        topBar = {
            TopBar(
                title = "Connect to addon",
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                rightButton = TopBarButton(
                    icon = Icons.Filled.Close,
                    description = "Close",
                    onClick = onExit
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { padding ->
        Column (
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.PaddingScaffoldContent)
        ) {
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                Text("Scan addon QR code :")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(width = 300.dp, height = 400.dp)
                ) {
                    if (hasCameraPermission) {
                        QrScanner(onQrCodeScanned = { onAddressSubmit(it) })
                    } else {
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(
//                                context as android.app.Activity,
//                                android.Manifest.permission.CAMERA
//                            )
//                        ) {
//                            Dialog({}) {
//                                Surface {
//                                    Text("Camera permission is needed to scan QR codes. Alternatively, the host IP address can be entered manually.")
//                                }
//                            }
//                        }
                        Surface (
                            color = Color.LightGray,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                }
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxHeight()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                                Text(
                                    "Camera permission not granted",
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                        if (!askedForPermission) {
                            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            askedForPermission = true
                        }
                    }
                }
                Text("\nOr enter host address manually :")
            } else {
                Text("Enter host address :")
            }
            OutlinedTextField(
                value = addressInput,
                onValueChange = { addressInput = it },
                placeholder = { Text("IP:Port") },
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onAddressSubmit(addressInput) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}