package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.CalculatorMode

@Composable
fun ModeSelector(
    currentMode: CalculatorMode,
    onModeChange: (CalculatorMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val nextMode = when (currentMode) {
        CalculatorMode.STANDARD -> CalculatorMode.SCIENTIFIC
        CalculatorMode.SCIENTIFIC -> CalculatorMode.PROGRAMMER
        CalculatorMode.PROGRAMMER -> CalculatorMode.STANDARD
    }

    val modeIcon = when (currentMode) {
        CalculatorMode.STANDARD -> Icons.Filled.Calculate
        CalculatorMode.SCIENTIFIC -> Icons.Filled.Science
        CalculatorMode.PROGRAMMER -> Icons.Filled.DataObject
    }

    val modeDescription = when (currentMode) {
        CalculatorMode.STANDARD -> "Current mode: Standard"
        CalculatorMode.SCIENTIFIC -> "Current mode: Scientific"
        CalculatorMode.PROGRAMMER -> "Current mode: Programmer"
    }

    TopIconButton(
        icon = modeIcon,
        contentDescription = modeDescription,
        onClick = { onModeChange(nextMode) },
        modifier = modifier
    )
}

@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode
    val description = if (isDarkTheme) "Current theme: Dark" else "Current theme: Light"

    TopIconButton(
        icon = icon,
        contentDescription = description,
        onClick = onToggleTheme,
        modifier = modifier
    )
}

@Composable
fun InfoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopIconButton(
        icon = Icons.Filled.Info,
        contentDescription = "Project information",
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun TopIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonSize: Dp = 56.dp
    val bgNormal = MaterialTheme.colorScheme.surface
    val bgPressed = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val iconTint = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.size(buttonSize),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
        color = if (isPressed) bgPressed else bgNormal,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(28.dp),
                tint = iconTint
            )
        }
    }
}
