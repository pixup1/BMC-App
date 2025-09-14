package com.bmc.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.bmc.app.models.Settings
import com.bmc.app.ui.components.TopBar
import com.bmc.app.ui.components.TopBarButton
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
                        valueRange = 0.0f..10.0f,
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

@Composable
private fun SettingsSwitch(
    text: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column (
            modifier = Modifier
                .weight(1f)
                .padding(end = Dimens.PaddingSettingsSwitch)
                .padding(vertical = Dimens.PaddingSettingsItems / 2)
        ) {
            Text(
                text = text
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsSlider(
    text: String,
    description: String? = null,
    value: Float = 0f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.PaddingSettingsItems / 2)
    ) {
        Text(
            text = text
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = Dimens.PaddingSettingsSlider)
        ) {
            Slider (
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Dimens.PaddingSettingsSliderValue)
            )
            Text(
                text = String.format("%.1f", value),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsItemsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = Dimens.PaddingSettingsItems / 2)
    )
}

@Preview
@Composable
fun SettingsPagePreview() {
    SettingsPage(
        closeSettings = { }
    )
}