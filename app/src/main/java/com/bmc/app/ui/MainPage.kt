package com.bmc.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.bmc.app.ui.models.ConnectionState
import com.bmc.app.ui.theme.Dimens

@Composable
fun MainPage(
    openSettingsPage: () -> Unit,
    openConnectionPage: () -> Unit,
    connectionState: ConnectionState,
    onDisconnect: () -> Unit,
    sensorData: FloatArray,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                openSettings = openSettingsPage
            )
        },
        bottomBar = {
            BottomBar(
                connectionState = connectionState,
                onDisconnect = onDisconnect,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        modifier = modifier
    ) { padding ->
        if (connectionState is ConnectionState.Connected) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Dimens.PaddingScaffoldContent)
            ) {
                Text("Rotation:")
                Text("X=${sensorData[0]}", color = Color.Red)
                Text("Y=${sensorData[1]}", color = Color.Green)
                Text("Z=${sensorData[2]}", color = Color.Blue)
            }
        } else {
            Box (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Dimens.PaddingScaffoldContent)
            ) {
                Button(
                    onClick = openConnectionPage,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text("Connect to addon")
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier,
    openSettings: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "BMC",
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = openSettings,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings"
            )
        }
    }
}

@Composable
private fun BottomBar(
    connectionState: ConnectionState,
    onDisconnect: () -> Unit,
    modifier: Modifier
) {
    @Composable
    fun disconnectButton() {
        IconButton(
            onClick = onDisconnect
        ) {
            Icon(
                imageVector = Icons.Rounded.Clear,
                contentDescription = "Disconnect"
            )
        }
    }

    Row {
        when (connectionState) {
            is ConnectionState.Connected -> {
                Text(
                    text = "Connected to ${connectionState.host}",
                    color = Color.Green,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                disconnectButton()
            }

            is ConnectionState.Connecting -> {
                Text(
                    text = "Connecting to ${connectionState.host}...",
                    color = Color.Yellow,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                disconnectButton()
            }

            is ConnectionState.Disconnected -> {
                Text(
                    text = "Disconnected",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Preview
@Composable
fun MainPagePreview() {
    MainPage(
        openSettingsPage = {},
        openConnectionPage = {},
        connectionState = ConnectionState.Connected("host"),
        onDisconnect = {},
        sensorData = floatArrayOf(0f, 0f, 0f)
    )
}