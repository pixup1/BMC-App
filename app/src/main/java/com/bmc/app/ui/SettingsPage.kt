package com.bmc.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bmc.app.R
import com.bmc.app.models.Settings
import com.bmc.app.ui.components.TopBar
import com.bmc.app.ui.components.TopBarButton
import com.bmc.app.ui.components.SettingsItemsDivider
import com.bmc.app.ui.components.SettingsSlider
import com.bmc.app.ui.components.SettingsSwitch
import com.bmc.app.ui.theme.Dimens
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun SettingsPage(
    closeSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val useArcoreFlow = context.settingsDataStore.data
        .map { data: Settings -> data.useArcore }
    val useArcore by useArcoreFlow.collectAsState(initial = false)
    val useAccelerometerFlow = context.settingsDataStore.data
        .map { data: Settings -> data.useAccelerometer }
    val useAcceleratometer by useAccelerometerFlow.collectAsState(initial = false)
    val accelerometerCutoffFlow = context.settingsDataStore.data
        .map { data: Settings -> data.accelerometerCutoff }
    val accelerometerCutoff by accelerometerCutoffFlow.collectAsState(initial = 1.0f)

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.settings),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                leftButton = TopBarButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    description = stringResource(R.string.close_settings),
                    onClick = closeSettings
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.PaddingScaffoldContent)
                .verticalScroll(rememberScrollState())
        ) {
            val unimplementedMessage = stringResource(R.string.unimplemented_feature)
            SettingsSwitch(
                text = stringResource(R.string.use_arcore),
                description = stringResource(R.string.use_arcore_description),
                checked = false, //useArcore,
                onCheckedChange = {
                    scope.launch {
                        snackbarHostState.showSnackbar(unimplementedMessage)
//                        context.settingsDataStore.updateData { currentSettings ->
//                            currentSettings.toBuilder()
//                                .setUseArcore(it)
//                                .build()
//                        }
                    }
                }
            )
            AnimatedVisibility(
                visible = !useArcore
            ) {
                Column {
                    SettingsItemsDivider()
                    SettingsSwitch(
                        text = stringResource(R.string.use_accelerometer),
                        description = stringResource(R.string.use_accelerometer_description),
                        checked = useAcceleratometer,
                        onCheckedChange = {
                            scope.launch {
                                context.settingsDataStore.updateData { currentSettings ->
                                    currentSettings.toBuilder()
                                        .setUseAccelerometer(it)
                                        .build()
                                }
                            }
                        }
                    )
                }
            }
            AnimatedVisibility(
                visible = useAcceleratometer && !useArcore
            ) {
                Column {
                    SettingsItemsDivider()
                    SettingsSlider(
                        text = stringResource(R.string.accelerometer_speed_cutoff),
                        description = stringResource(R.string.accelerometer_speed_cutoff_description),
                        value = accelerometerCutoff,
                        valueRange = 0.0f..5.0f,
                        onValueChange = {
                            scope.launch {
                                context.settingsDataStore.updateData { currentSettings ->
                                    currentSettings.toBuilder()
                                        .setAccelerometerCutoff(it)
                                        .build()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SettingsPagePreview() {
    SettingsPage(
        closeSettings = { }
    )
}