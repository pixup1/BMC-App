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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
    val context = LocalContext.current
    val useAccelerometerFlow = context.settingsDataStore.data
        .map { data: Settings -> data.useAccelerometer }
    val useAcceleratometer by useAccelerometerFlow.collectAsState(initial = false)
    val accelerometerCutoffFlow = context.settingsDataStore.data
        .map { data: Settings -> data.accelerometerCutoff }
    val accelerometerCutoff by accelerometerCutoffFlow.collectAsState(initial = 1.0f) // TODO: find a way to define a default value other than 0 (why is protobuf like this ffs)

    Scaffold(
        topBar = {
            TopBar(
                title = "Settings",
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                leftButton = TopBarButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    description = "Close settings",
                    onClick = closeSettings
                )
            )
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
            SettingsSwitch(
                text = "Use Accelerometer",
                description = "Use device accelerometer for position data (imprecise)",
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
            AnimatedVisibility(
                visible = useAcceleratometer
            ) {
                Column {
                    SettingsItemsDivider()
                    SettingsSlider(
                        text = "Accelerometer Speed Cutoff",
                        description = "Higher values will reduce drift but also fine movement",
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