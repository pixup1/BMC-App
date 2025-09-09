package com.bmc.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.bmc.app.ui.components.TopBar
import com.bmc.app.ui.components.TopBarButton
import com.bmc.app.models.ConnectionState
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
                title = "BMC",
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                rightButton = TopBarButton(
                    icon = Icons.Filled.Settings,
                    description = "Settings",
                    onClick = openSettingsPage
                )
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
                    .padding(Dimens.PaddingScaffoldContent)
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
private fun BottomBar(
    connectionState: ConnectionState,
    onDisconnect: () -> Unit,
    modifier: Modifier
) {
    @Composable
    fun ConnectionStatePill(
        text: String,
        color: Color,
        showDisconnectButton: Boolean
    ) {
        Box (
            modifier = modifier
                .height(Dimens.HeightBottomBarContent + 2 * Dimens.PaddingBottomBar)
                .padding(Dimens.PaddingBottomBar)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxHeight()
                    .drawBehind {
                        drawRoundRect(
                            color = color,
                            cornerRadius = CornerRadius((Dimens.HeightBottomBarContent / 50.dp * 12.dp).toPx())
                        )
                    }
                    .padding(
                        start = Dimens.HeightBottomBarContent / 50.dp * 18.dp,
                        end = Dimens.HeightBottomBarContent / 50.dp *
                                if (showDisconnectButton) {14.dp}
                                else {18.dp}
                    )
            ) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
                if (showDisconnectButton) {
                    Box(Modifier.size(Dimens.HeightBottomBarContent / 50.dp * 4.dp))
                    val iconButtonSize = 30.dp
                    IconButton(
                        onClick = onDisconnect,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(Dimens.HeightBottomBarContent / 50.dp * iconButtonSize)

                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            tint = Color.Black,
                            contentDescription = "Disconnect",
                            modifier = Modifier.size(Dimens.HeightBottomBarContent / 50.dp * iconButtonSize)
                        )
                    }
                }
            }
        }
    }

    HorizontalDivider()
    when (connectionState) {
        is ConnectionState.Connected -> {
            ConnectionStatePill(
                text = "Connected to ${connectionState.host}",
                color = Color.Green,
                showDisconnectButton = true
            )
        }

        is ConnectionState.Connecting -> {
            ConnectionStatePill(
                text = "Connecting to host...",
                color = Color.Yellow,
                showDisconnectButton = true
            )
        }

        is ConnectionState.Disconnected -> {
            ConnectionStatePill(
                text = "Disconnected",
                color = Color.Red,
                showDisconnectButton = false
            )
        }
    }
}

@Preview
@Composable
fun MainPagePreview() {
    MainPage(
        openSettingsPage = {},
        openConnectionPage = {},
        connectionState = ConnectionState.Connected("127.0.0.1"),
        onDisconnect = {},
        sensorData = floatArrayOf(0f, 0f, 0f)
    )
}