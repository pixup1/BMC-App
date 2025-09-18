package com.bmc.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.bmc.app.ui.theme.Dimens

data class TopBarButton(
    val icon: ImageVector,
    val description: String?,
    val onClick: () -> Unit
)

@Composable
fun TopBar (
    title: String?,
    leftButton: TopBarButton? = null,
    rightButton: TopBarButton? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.HeightTopBar)
    ) {
        if (title != null) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        if (leftButton != null) {
            IconButton(
                onClick = leftButton.onClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = leftButton.icon,
                    contentDescription = leftButton.description,
                )
            }
        }
        if (rightButton != null) {
            IconButton(
                onClick = rightButton.onClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = rightButton.icon,
                    contentDescription = rightButton.description,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}