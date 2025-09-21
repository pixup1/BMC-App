package com.bmc.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ControlCamera
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.bmc.app.R
import com.bmc.app.ui.components.TopBar
import com.bmc.app.ui.components.TopBarButton
import com.bmc.app.models.ConnectionState
import com.bmc.app.models.Settings
import com.bmc.app.ui.components.SettingsSwitch
import com.bmc.app.ui.theme.Dimens
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainPage(
    openSettingsPage: () -> Unit,
    openConnectionPage: () -> Unit,
    resetTransform: () -> Unit,
    lockAccelerometer: () -> Unit,
    lockGyroscope: () -> Unit,
    unlockAccelerometer: () -> Unit,
    unlockGyroscope: () -> Unit,
    connectionState: ConnectionState,
    onDisconnect: () -> Unit,
    dataMuted: Boolean = false,
    onDataMutedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val useAccelerometerFlow = remember {
        context.settingsDataStore.data.map { data: Settings -> data.useAccelerometer }
    }
    val useAccelerometer by useAccelerometerFlow.collectAsState(initial = false)

    val absoluteRotationFlow = remember {
        context.settingsDataStore.data.map { data: Settings -> data.absoluteRotation }
    }
    val absoluteRotation by absoluteRotationFlow.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.title_activity_main),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                rightButton = TopBarButton(
                    icon = Icons.Filled.Settings,
                    description = stringResource(R.string.settings),
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
        when (connectionState) {
            is ConnectionState.Connected -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(Dimens.PaddingScaffoldContent)
                ) {
                    Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
                        SettingsSwitch(
                            text = stringResource(R.string.mute),
                            description = stringResource(R.string.mute_description),
                            checked = dataMuted,
                            onCheckedChange = {
                                onDataMutedChange(it)
                            }
                        )
                    }
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            AnimatedVisibility(visible = useAccelerometer || (!absoluteRotation)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.reset_transform),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = Dimens.PaddingBetweenMainButtons)
                                    )
                                    Button(
                                        onClick = resetTransform,
                                        shape = RoundedCornerShape(Dimens.RadiusMainButtons),
                                        modifier = Modifier
                                            .size(Dimens.SizeMainButtons)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Repeat,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                            AnimatedVisibility(visible = useAccelerometer) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = Dimens.PaddingBetweenMainButtons)
                                ) {
                                    Text(
                                        text = stringResource(R.string.lock_translation),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = Dimens.PaddingBetweenMainButtons)
                                    )
                                    HoldButton(
                                        onPress = lockAccelerometer,
                                        onRelease = unlockAccelerometer,
                                        shape = RoundedCornerShape(Dimens.RadiusMainButtons),
                                        modifier = Modifier
                                            .size(Dimens.SizeMainButtons)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ControlCamera,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                            AnimatedVisibility(visible = !absoluteRotation) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = Dimens.PaddingBetweenMainButtons)
                                ) {
                                    Text(
                                        text = stringResource(R.string.lock_rotation),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = Dimens.PaddingBetweenMainButtons)
                                    )
                                    HoldButton(
                                        onPress = lockGyroscope,
                                        onRelease = unlockGyroscope,
                                        shape = RoundedCornerShape(Dimens.RadiusMainButtons),
                                        modifier = Modifier
                                            .size(Dimens.SizeMainButtons)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FlipCameraAndroid,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is ConnectionState.Connecting -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
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
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = null
                        )
                        Box(Modifier.size(Dimens.PaddingButtonIcon))
                        Text(stringResource(R.string.connect_addon))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HoldButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }

    Button(
        onClick = {},
        shape = shape,
        interactionSource = interactionSource,
        modifier = modifier.pointerInteropFilter { motionEvent ->
            when (motionEvent.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    pressed = true
                    onPress()
                    true
                }

                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    pressed = false
                    onRelease()
                    true
                }

                else -> false
            }
        }
    ) {
        content()
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            interactionSource.emit(PressInteraction.Press(Offset.Zero))
        } else {
            interactionSource.emit(PressInteraction.Release(PressInteraction.Press(Offset.Zero)))
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
                            contentDescription = stringResource(R.string.disconnect),
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
                text = stringResource(R.string.connected_to, connectionState.host),
                color = Color.Green,
                showDisconnectButton = true
            )
        }

        is ConnectionState.Connecting -> {
            ConnectionStatePill(
                text = stringResource(R.string.connecting),
                color = Color.Yellow,
                showDisconnectButton = true
            )
        }

        is ConnectionState.Disconnected -> {
            ConnectionStatePill(
                text = stringResource(R.string.disconnected),
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
        //connectionState = ConnectionState.Disconnected,
        resetTransform = {},
        lockGyroscope = {},
        lockAccelerometer = {},
        unlockGyroscope = {},
        unlockAccelerometer = {},
        dataMuted = false,
        onDataMutedChange = {},
        onDisconnect = {}
    )
}