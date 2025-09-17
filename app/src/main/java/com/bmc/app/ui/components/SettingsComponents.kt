package com.bmc.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.bmc.app.ui.theme.Dimens

@Composable
fun SettingsSwitch(
    text: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Column (
            modifier = Modifier
                .weight(1f)
                .padding(end = Dimens.PaddingSettingsSwitch)
                .padding(vertical = Dimens.PaddingSettingsItems / 2)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
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
fun SettingsSlider(
    text: String,
    description: String? = null,
    value: Float = 0f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(vertical = Dimens.PaddingSettingsItems / 2)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
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
fun SettingsItemsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = Dimens.PaddingSettingsItems / 2)
    )
}
