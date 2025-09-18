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
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.bmc.app.models.ConnectionPageData
import com.bmc.app.models.ConnectionPageDataSerializer
import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bmc.app.R
import com.bmc.app.ui.components.QrScanner
import com.bmc.app.ui.components.TopBar
import com.bmc.app.ui.components.TopBarButton
import com.bmc.app.ui.theme.Dimens
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.connectionPageDataStore: DataStore<ConnectionPageData> by dataStore(
    fileName = "connection_page_data.pb",
    serializer = ConnectionPageDataSerializer
)

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

    val recentHostsFlow = context.connectionPageDataStore.data
        .map { data: ConnectionPageData -> data.recentHostsList }
    val recentHosts by recentHostsFlow.collectAsState(initial = emptyList())

    var addressInput by remember { mutableStateOf("") }
    var isInputError by remember { mutableStateOf(false) }
    var askedForPermission by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun connect(address: String) {
        onAddressSubmit(address)
        scope.launch { //TODO: only do this if connection is successful
            context.connectionPageDataStore.updateData { currentData ->
                val updatedRecentHosts = listOf(address) + currentData.recentHostsList.filter { it != address }
                currentData.toBuilder()
                    .clearRecentHosts()
                    .addAllRecentHosts(updatedRecentHosts.take(5)) // Keep only the 5 most recent unique hosts
                    .build()
            }
        }
    }

    val permissionMessage = stringResource(R.string.camera_permission_needed)
    val requestPermissionLauncher = rememberLauncherForActivityResult (
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        hasCameraPermission = granted
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar(permissionMessage)
            }
        }
    }

    Scaffold (
        topBar = {
            TopBar(
                title = stringResource(R.string.connect_addon),
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
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.PaddingScaffoldContent)
        ) {
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                Text(stringResource(R.string.scan_qr_code))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(width = 300.dp, height = 400.dp)
                ) {
                    if (hasCameraPermission) {
                        val pleaseMessage = stringResource(R.string.please_scan_bmc_qr_code)
                        QrScanner(onQrCodeScanned = { //TODO: fix QrScanner
                            try {
                                connect(addressInput)
                            } catch (_: IllegalArgumentException) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(pleaseMessage)
                                }
                            }
                        })
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
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data =
                                                Uri.fromParts("package", context.packageName, null)
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
                                    stringResource(R.string.camera_permission_not_granted),
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
                Text("\n" + stringResource(R.string.or_enter_host_address))
            } else {
                Text(stringResource(R.string.enter_host_address))
            }

            OutlinedTextField(
                value = addressInput,
                onValueChange = {
                    addressInput = it
                    isInputError = false
                                },
                placeholder = { Text(stringResource(R.string.ip_port)) },
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        try {
                            connect(addressInput)
                        } catch (_: IllegalArgumentException) {
                            isInputError = true
                        }
                    }
                ),
                singleLine = true,
                isError = isInputError,
                label = { if (isInputError) Text(stringResource(R.string.invalid_address_format)) else null },
                modifier = Modifier.fillMaxWidth()
            )

            if (!recentHosts.isEmpty()) {
                Text("\n" + stringResource(R.string.recent_connections))

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    recentHosts.forEach { host ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box (
                                modifier = Modifier
                                    .height(Dimens.HeightRecentConnectionsItem)
                                    .weight(1f)
                                    .clickable {
                                        addressInput = host
                                        onAddressSubmit(host)
                                    }
                            ) {
                                Text(
                                    host,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                )
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        context.connectionPageDataStore.updateData { currentData ->
                                            currentData.toBuilder()
                                                .clearRecentHosts()
                                                .addAllRecentHosts(currentData.recentHostsList.filter { it != host })
                                                .build()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .size(Dimens.RecentConnectionsDelete)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    tint = Color.Black,
                                    contentDescription = stringResource(R.string.forget_host),
                                    modifier = Modifier.size(Dimens.RecentConnectionsDelete)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ConnectionPagePreview() {
    ConnectionPage(
        onExit = {},
        onAddressSubmit = {}
    )
}