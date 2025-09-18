package com.bmc.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bmc.app.models.Settings
import com.bmc.app.ui.components.TopBar
import com.bmc.app.ui.components.TopBarButton
import com.bmc.app.ui.theme.Dimens
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun AxisDialog(
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val topAxisFlow = context.settingsDataStore.data
        .map { data: Settings -> data.topAxis }
    val topAxis by topAxisFlow.collectAsState(initial = 2)
    val rightAxisFlow = context.settingsDataStore.data
        .map { data: Settings -> data.rightAxis }
    val rightAxis by rightAxisFlow.collectAsState(initial = 1)

    var rightMenuOpen by remember { mutableStateOf(false) }
    var topMenuOpen by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = RoundedCornerShape(Dimens.RadiusDialog),
        ) {
            Column {
                TopBar(
                    title = "Pick Axes",
                    rightButton = TopBarButton(
                        icon = Icons.Rounded.Close,
                        description = "Close dialog",
                        onClick = onDismissRequest
                    )
                )
                Spacer(modifier = Modifier.size(Dimens.SizeAxisDialogArrows / 4))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box (
                        contentAlignment = Alignment.BottomCenter,
                        modifier = Modifier.size(Dimens.SizeAxisDialogArrows)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = getAxisColor(topAxis),
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = getAxisColor(topAxis),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable(onClick = {
                                    topMenuOpen = !topMenuOpen
                                })
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = when (topAxis) {
                                        1 -> " X+"
                                        2 -> " Y+"
                                        3 -> " Z+"
                                        4 -> " X-"
                                        5 -> " Y-"
                                        6 -> " Z-"
                                        else -> " ?"
                                    },
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        AxisDropdownMenu(
                            expanded = topMenuOpen,
                            onDismissRequest = { topMenuOpen = false },
                            onSelect = {
                                topMenuOpen = false
                                scope.launch {
                                    context.settingsDataStore.updateData { currentSettings ->
                                        currentSettings.toBuilder()
                                            .setTopAxis(it)
                                            .build()
                                    }
                                }
                            },
                            otherAxis = rightAxis
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.size(Dimens.SizeAxisDialogArrows))
                    Icon(
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.SizeAxisDialogPhone)
                    )
                    Box (
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .size(Dimens.SizeAxisDialogArrows)
                            .offset(x = -Dimens.SizeAxisDialogPhone / 10, y = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = getAxisColor(rightAxis),
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = getAxisColor(rightAxis),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(4.dp)
                                .offset(x = -Dimens.SizeAxisDialogPhone / 15, y = 0.dp)
                                .clickable(onClick = {
                                    rightMenuOpen = !rightMenuOpen
                                })
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = when (rightAxis) {
                                        1 -> " X+"
                                        2 -> " Y+"
                                        3 -> " Z+"
                                        4 -> " X-"
                                        5 -> " Y-"
                                        6 -> " Z-"
                                        else -> " ?"
                                    },
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        AxisDropdownMenu(
                            expanded = rightMenuOpen,
                            onDismissRequest = { rightMenuOpen = false },
                            onSelect = {
                                rightMenuOpen = false
                                scope.launch {
                                    context.settingsDataStore.updateData { currentSettings ->
                                        currentSettings.toBuilder()
                                            .setRightAxis(it)
                                            .build()
                                    }
                                }
                            },
                            otherAxis = topAxis
                        )
                    }
                }
                Text(
                    text = "Make the axes match the local space of the object.\n\nThird axis will be inferred.",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(Dimens.SizeAxisDialogArrows / 2)
                )
            }
        }
    }
}

@Composable
private fun AxisDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (Int) -> Unit,
    otherAxis: Int
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        if (otherAxis != 1 && otherAxis != 4) {
            DropdownMenuItem(
                text = { Text(" X+") },
                onClick = { onSelect(1) }
            )
            DropdownMenuItem(
                text = { Text(" X-") },
                onClick = { onSelect(4) }
            )
        }
        if (otherAxis != 2 && otherAxis != 5) {
            DropdownMenuItem(
                text = { Text(" Y+") },
                onClick = { onSelect(2) }
            )
            DropdownMenuItem(
                text = { Text(" Y-") },
                onClick = { onSelect(5) }
            )
        }
        if (otherAxis != 3 && otherAxis != 6) {
            DropdownMenuItem(
                text = { Text(" Z+") },
                onClick = { onSelect(3) }
            )
            DropdownMenuItem(
                text = { Text(" Z-") },
                onClick = { onSelect(6) }
            )
        }
    }
}

private fun getAxisColor(axis: Int): Color {
    return when (axis) {
        1, 4 -> Color.hsl(0f, 0.7f, 0.55f)
        2, 5 -> Color.hsl(120f, 0.7f, 0.55f)
        3, 6 -> Color.hsl(240f, 0.7f, 0.55f)
        else -> Color.Gray
    }
}

@Preview
@Composable
fun AxisDialogPreview() {
    AxisDialog(
        onDismissRequest = {}
    )
}