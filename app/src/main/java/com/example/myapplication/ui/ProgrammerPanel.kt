package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import com.example.myapplication.model.CalculatorAction
import com.example.myapplication.model.ProgrammerInputPane
import com.example.myapplication.model.ProgrammerShiftMode

@Composable
fun ProgrammerPanel(
    value: Long,
    currentBase: Int,
    wordSize: Int,
    inputPane: ProgrammerInputPane,
    shiftMode: ProgrammerShiftMode,
    onBaseChange: (Int) -> Unit,
    onWordSizeChange: (Int) -> Unit,
    onToggleInputPane: () -> Unit,
    onBitwiseAction: (CalculatorAction) -> Unit,
    onShiftModeChange: (ProgrammerShiftMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        listOf(
            Triple("HEX", 16, formatProgrammerValue(value, 16, wordSize)),
            Triple("DEC", 10, formatProgrammerValue(value, 10, wordSize)),
            Triple("OCT", 8, formatProgrammerValue(value, 8, wordSize)),
            Triple("BIN", 2, formatProgrammerValue(value, 2, wordSize))
        ).forEach { (label, base, formatted) ->
            ProgrammerBaseRow(
                label = label,
                value = formatted,
                selected = currentBase == base
            ) {
                onBaseChange(base)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeyboardModeToggleButton(
                inputPane = inputPane,
                onToggleInputPane = onToggleInputPane
            )

            ProgrammerWordSizeMenu(
                wordSize = wordSize,
                onWordSizeChange = onWordSizeChange
            )

            ProgrammerBitwiseMenu(
                onBitwiseAction = onBitwiseAction,
                modifier = Modifier.weight(1f)
            )

            ProgrammerShiftMenu(
                shiftMode = shiftMode,
                onShiftModeChange = onShiftModeChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProgrammerBaseRow(
    label: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(
                    color = if (selected) Color(0, 101, 179) else Color.Transparent,
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Text(
            text = label,
            fontSize = 17.sp,
            modifier = Modifier
                .width(54.dp)
                .padding(start = 12.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun KeyboardModeToggleButton(
    inputPane: ProgrammerInputPane,
    onToggleInputPane: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val containerColor = if (isDarkTheme) Color(0, 52, 114) else Color(245, 245, 245)
    val iconColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface

    val icon = if (inputPane == ProgrammerInputPane.KEYPAD) {
        Icons.Filled.Dialpad
    } else {
        Icons.Filled.Apps
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        modifier = Modifier.clickable(onClick = onToggleInputPane)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Toggle programmer input mode",
            modifier = Modifier
                .size(32.dp)
                .padding(6.dp),
            tint = iconColor
        )
    }
}

@Composable
private fun ProgrammerMenuChip(
    label: String,
    leading: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val containerColor = if (isDarkTheme) Color(0, 52, 114) else Color(245, 245, 245)
    val primaryTextColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = leading,
                fontSize = 14.sp,
                color = secondaryTextColor
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = primaryTextColor
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = secondaryTextColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ProgrammerBitwiseMenu(
    onBitwiseAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        ProgrammerMenuChip(
            label = "\u6309\u4f4d",
            leading = "\u22c4",
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BitwiseActionButton("AND") {
                        expanded = false
                        onBitwiseAction(CalculatorAction.And)
                    }
                    BitwiseActionButton("OR") {
                        expanded = false
                        onBitwiseAction(CalculatorAction.Or)
                    }
                    BitwiseActionButton("NOT") {
                        expanded = false
                        onBitwiseAction(CalculatorAction.Not)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BitwiseActionButton("NAND") {
                        expanded = false
                        onBitwiseAction(CalculatorAction.Nand)
                    }
                    BitwiseActionButton("NOR") {
                        expanded = false
                        onBitwiseAction(CalculatorAction.Nor)
                    }
                    BitwiseActionButton("XOR") {
                        expanded = false
                        onBitwiseAction(CalculatorAction.Xor)
                    }
                }
            }
        }
    }
}

@Composable
private fun BitwiseActionButton(
    label: String,
    onClick: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val containerColor = if (isDarkTheme) Color(0, 52, 114) else Color(250, 250, 250)
    val textColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = Modifier
            .width(102.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = textColor
            )
        }
    }
}

@Composable
private fun ProgrammerWordSizeMenu(
    wordSize: Int,
    onWordSizeChange: (Int) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val containerColor = if (isDarkTheme) Color(0, 52, 114) else Color(245, 245, 245)
    val textColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface

    val current = when (wordSize) {
        64 -> "QWORD"
        32 -> "DWORD"
        16 -> "WORD"
        else -> "BYTE"
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.clickable {
            val next = when (wordSize) {
                64 -> 32
                32 -> 16
                16 -> 8
                else -> 64
            }
            onWordSizeChange(next)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = current,
                fontSize = 14.sp,
                color = textColor
            )
        }
    }
}

@Composable
private fun ProgrammerShiftMenu(
    shiftMode: ProgrammerShiftMode,
    onShiftModeChange: (ProgrammerShiftMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        ProgrammerShiftMode.ARITHMETIC to "\u7b97\u672f\u79fb\u4f4d",
        ProgrammerShiftMode.LOGICAL to "\u903b\u8f91\u79fb\u4f4d",
        ProgrammerShiftMode.ROTATE to "\u65cb\u8f6c\u5faa\u73af\u79fb\u4f4d",
        ProgrammerShiftMode.ROTATE_WITH_CARRY to "\u5e26\u8fdb\u4f4d\u65cb\u8f6c\u5faa\u73af\u79fb\u4f4d"
    )

    Box(modifier = modifier) {
        ProgrammerMenuChip(
            label = "\u4f4d\u79fb\u4f4d",
            leading = "\u226b",
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (mode, label) ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = mode == shiftMode,
                                onClick = null
                            )
                            Text(label)
                        }
                    },
                    onClick = {
                        expanded = false
                        onShiftModeChange(mode)
                    }
                )
            }
        }
    }
}

private fun formatProgrammerValue(value: Long, base: Int, wordSize: Int): String {
    val masked = applyWordMaskForDisplay(value, wordSize)
    val unsigned = toUnsignedWithinWordSize(masked, wordSize)
    return when (base) {
        16 -> unsigned.toString(16).uppercase()
        10 -> masked.toString()
        8 -> unsigned.toString(8)
        2 -> formatBinaryGroups(unsigned)
        else -> masked.toString()
    }
}

private fun applyWordMaskForDisplay(value: Long, wordSize: Int): Long {
    return when (wordSize) {
        8 -> value.toByte().toLong()
        16 -> value.toShort().toLong()
        32 -> value.toInt().toLong()
        else -> value
    }
}

private fun toUnsignedWithinWordSize(value: Long, wordSize: Int): ULong {
    return when (wordSize) {
        8 -> (value and 0xFFL).toULong()
        16 -> (value and 0xFFFFL).toULong()
        32 -> (value and 0xFFFFFFFFL).toULong()
        else -> value.toULong()
    }
}

private fun formatBinaryGroups(unsigned: ULong): String {
    val raw = unsigned.toString(2)
    val trimmed = raw.trimStart('0').ifEmpty { "0" }
    if (trimmed == "0") return "0"
    val paddedLength = maxOf(4, ((trimmed.length + 3) / 4) * 4)
    return trimmed
        .padStart(paddedLength, '0')
        .chunked(4)
        .joinToString(" ")
}
