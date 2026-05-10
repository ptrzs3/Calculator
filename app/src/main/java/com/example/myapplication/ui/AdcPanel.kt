package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.AdcDirection
import com.example.myapplication.model.AdcEncoding
import com.example.myapplication.reducer.adcPresets
import com.example.myapplication.reducer.formatVoltage
import com.example.myapplication.reducer.lsbVoltage
import com.example.myapplication.reducer.maxCode
import com.example.myapplication.reducer.signExtend

@Composable
fun AdcPanel(
    direction: AdcDirection,
    resolution: Int,
    vrefPlus: Double,
    vrefMinus: Double,
    encoding: AdcEncoding,
    digitalValue: Long,
    analogValue: Double,
    presetIndex: Int,
    onDirectionToggle: () -> Unit,
    onResolutionChange: (Int) -> Unit,
    onVrefPlusChange: (Double) -> Unit,
    onVrefMinusChange: (Double) -> Unit,
    onEncodingChange: (AdcEncoding) -> Unit,
    onApplyPreset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Direction toggle row
        DirectionToggleRow(direction = direction, onToggle = onDirectionToggle)

        // Conversion result
        ConversionResultRow(
            direction = direction,
            digitalValue = digitalValue,
            analogValue = analogValue,
            resolution = resolution
        )

        // Info row: LSB, quant error, max code
        InfoRow(
            vrefPlus = vrefPlus,
            vrefMinus = vrefMinus,
            resolution = resolution,
            encoding = encoding
        )

        // Config row
        ConfigRow(
            resolution = resolution,
            vrefPlus = vrefPlus,
            vrefMinus = vrefMinus,
            encoding = encoding,
            presetIndex = presetIndex,
            onResolutionChange = onResolutionChange,
            onVrefPlusChange = onVrefPlusChange,
            onVrefMinusChange = onVrefMinusChange,
            onEncodingChange = onEncodingChange,
            onApplyPreset = onApplyPreset
        )
    }
}

@Composable
private fun DirectionToggleRow(
    direction: AdcDirection,
    onToggle: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdcDirectionChip(
            label = "D→A",
            description = "Digital to Analog",
            selected = direction == AdcDirection.DIGITAL_TO_ANALOG,
            onClick = {
                if (direction != AdcDirection.DIGITAL_TO_ANALOG) onToggle()
            },
            modifier = Modifier.weight(1f)
        )
        AdcDirectionChip(
            label = "A→D",
            description = "Analog to Digital",
            selected = direction == AdcDirection.ANALOG_TO_DIGITAL,
            onClick = {
                if (direction != AdcDirection.ANALOG_TO_DIGITAL) onToggle()
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AdcDirectionChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val selectedBg = if (isDarkTheme) Color(0, 101, 179) else Color(0, 90, 158)
    val unselectedBg = if (isDarkTheme) Color(32, 32, 32) else Color(245, 245, 245)
    val selectedText = Color.White
    val unselectedText = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (selected) selectedBg else unselectedBg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = if (selected) selectedText else unselectedText
            )
        }
    }
}

@Composable
private fun ConversionResultRow(
    direction: AdcDirection,
    digitalValue: Long,
    analogValue: Double,
    resolution: Int
) {
    val codeHex = digitalValue.toString(16).uppercase().padStart(
        ((resolution + 3) / 4).coerceAtLeast(1), '0'
    )
    val textColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (direction) {
                AdcDirection.DIGITAL_TO_ANALOG -> "0x$codeHex → ${formatVoltage(analogValue)} V"
                AdcDirection.ANALOG_TO_DIGITAL -> "${formatVoltage(analogValue)} V → 0x$codeHex"
            },
            fontSize = 18.sp,
            color = textColor
        )
    }
}

@Composable
private fun InfoRow(
    vrefPlus: Double,
    vrefMinus: Double,
    resolution: Int,
    encoding: AdcEncoding
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDarkTheme) Color(32, 32, 32) else Color(245, 245, 245)
    val textColor = if (isDarkTheme) Color.White.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant

    val lsb = lsbVoltage(vrefPlus, vrefMinus, resolution)
    val halfLsb = lsb / 2.0
    val max = maxCode(resolution)
    val maxHex = max.toString(16).uppercase()

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "LSB = ${formatVoltageSmall(lsb)} V   ±½ LSB = ±${formatVoltageSmall(halfLsb)} V",
                fontSize = 13.sp,
                color = textColor
            )
            Text(
                text = when (encoding) {
                    AdcEncoding.STRAIGHT_BINARY -> "Max Code = $max (0x$maxHex)"
                    AdcEncoding.TWOS_COMPLEMENT -> {
                        val halfMax = (max + 1) / 2
                        "Range = -$halfMax..${halfMax - 1} (0x${maxHex})"
                    }
                },
                fontSize = 13.sp,
                color = textColor
            )
        }
    }
}

@Composable
private fun ConfigRow(
    resolution: Int,
    vrefPlus: Double,
    vrefMinus: Double,
    encoding: AdcEncoding,
    presetIndex: Int,
    onResolutionChange: (Int) -> Unit,
    onVrefPlusChange: (Double) -> Unit,
    onVrefMinusChange: (Double) -> Unit,
    onEncodingChange: (AdcEncoding) -> Unit,
    onApplyPreset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdcDropdownChip(
            label = "${resolution}-bit",
            leading = "Res",
            modifier = Modifier.weight(1f),
            menuContent = {
                listOf(8, 10, 12, 14, 16, 20, 24).forEach { bits ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "$bits-bit",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = { onResolutionChange(bits) }
                    )
                }
            }
        )

        AdcDropdownChip(
            label = formatVoltage(vrefPlus),
            leading = "V+",
            modifier = Modifier.weight(1f),
            menuContent = {
                listOf(1.8, 2.5, 3.3, 5.0, 10.0).forEach { v ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${formatVoltage(v)} V",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = { onVrefPlusChange(v) }
                    )
                }
            }
        )

        AdcDropdownChip(
            label = formatVoltage(vrefMinus),
            leading = "V-",
            modifier = Modifier.weight(1f),
            menuContent = {
                listOf(0.0, -2.5, -5.0, -10.0).forEach { v ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${formatVoltage(v)} V",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = { onVrefMinusChange(v) }
                    )
                }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdcDropdownChip(
            label = encoding.displayName,
            leading = "Enc",
            modifier = Modifier.weight(1f),
            menuContent = {
                AdcEncoding.entries.forEach { enc ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                enc.displayName,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = { onEncodingChange(enc) }
                    )
                }
            }
        )

        val preset = adcPresets[presetIndex]
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                Color(32, 32, 32) else Color(245, 245, 245),
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onApplyPreset)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "▶",
                    fontSize = 14.sp,
                    color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                        Color.White.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = preset.name,
                    fontSize = 14.sp,
                    color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                        Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AdcDropdownChip(
    label: String,
    leading: String,
    modifier: Modifier = Modifier,
    menuContent: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val containerColor = if (isDarkTheme) Color(32, 32, 32) else Color(245, 245, 245)
    val menuContainerColor = if (isDarkTheme) Color(44, 44, 44) else MaterialTheme.colorScheme.surface
    val primaryTextColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.82f)
        else MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier) {
        Surface(
            color = containerColor,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = leading, fontSize = 13.sp, color = secondaryTextColor)
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = primaryTextColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = secondaryTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = menuContainerColor
        ) {
            menuContent()
        }
    }
}

private fun formatVoltageSmall(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    val absVal = kotlin.math.abs(value)
    return when {
        absVal == 0.0 -> "0"
        absVal >= 1.0 -> String.format("%.3f", value)
        absVal >= 0.001 -> String.format("%.1f", value * 1000.0) + "m"
        else -> String.format("%.1f", value * 1_000_000.0) + "μ"
    }
}
