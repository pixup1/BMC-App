package com.bmc.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bmc.app.ui.theme.Dimens

@Composable
fun SettingsPage(
    closeSettings: () -> Unit,
    modifier: Modifier = Modifier
) {


    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                closeSettings = closeSettings
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.PaddingScaffoldContent)
        ) {
            settingsItem(
                text = "Setting1",
                checked = false,
                onCheckedChange = { /* Handle change */ }
            )
            HorizontalDivider()
            settingsItem(
                text = "Setting2",
                checked = false,
                onCheckedChange = { /* Handle change */ }
            )
        }
    }
}

@Composable
private fun settingsItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun TopBar(
    modifier: Modifier,
    closeSettings: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Settings",
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = closeSettings,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
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