package com.bmc.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MainPage(
    openSettingsPage: () -> Unit,
    openConnectionPage: () -> Unit,
    //connectionStatus: null, TODO: Add connection status enum
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
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (1 == 0) { //TODO: Change to connection status
                Text("Rotation:")
                Text("X=${sensorData[0]}", color = Color.Red)
                Text("Y=${sensorData[1]}", color = Color.Green)
                Text("Z=${sensorData[2]}", color = Color.Blue)
            } else {
                Button(
                    onClick = openConnectionPage
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
        Button(
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
    modifier: Modifier
) {
    Text(
        "Status: Not connected",
        modifier = modifier
    )
}

@Preview
@Composable
fun MainPagePreview() {
    MainPage(
        openSettingsPage = {},
        openConnectionPage = {},
        sensorData = floatArrayOf(0f, 0f, 0f)
    )
}