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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.CalculatorAction
import com.example.myapplication.ui.theme.KeyEqualsDark
import com.example.myapplication.ui.theme.KeyEqualsLight
import com.example.myapplication.ui.theme.SegoeFont

@Composable
fun ScientificFunctionMenus(
    scientificTrigSecondEnabled: Boolean,
    scientificHypEnabled: Boolean,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val sinLabel = if (scientificTrigSecondEnabled) "sin\u207b\u00b9" else "sin"
    val cosLabel = if (scientificTrigSecondEnabled) "cos\u207b\u00b9" else "cos"
    val tanLabel = if (scientificTrigSecondEnabled) "tan\u207b\u00b9" else "tan"
    val secLabel = if (scientificTrigSecondEnabled) "sec\u207b\u00b9" else "sec"
    val cscLabel = if (scientificTrigSecondEnabled) "csc\u207b\u00b9" else "csc"
    val cotLabel = if (scientificTrigSecondEnabled) "cot\u207b\u00b9" else "cot"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScientificMenuSelector(
            symbol = "\u2221",
            title = "\u4e09\u89d2\u5b66",
            onAction = onAction,
            menuContent = { onMenuAction ->
                MenuGrid(
                    rows = listOf(
                        listOf(
                            MenuKey(
                                label = "2nd",
                                action = CalculatorAction.ToggleTrigSecond,
                                selected = scientificTrigSecondEnabled,
                                closeOnClick = false,
                                useEqualsSelectedColor = true
                            ),
                            MenuKey(sinLabel, CalculatorAction.Sin),
                            MenuKey(cosLabel, CalculatorAction.Cos),
                            MenuKey(tanLabel, CalculatorAction.Tan)
                        ),
                        listOf(
                            MenuKey("hyp", CalculatorAction.ToggleHyp, selected = scientificHypEnabled),
                            MenuKey(secLabel, CalculatorAction.Sec),
                            MenuKey(cscLabel, CalculatorAction.Csc),
                            MenuKey(cotLabel, CalculatorAction.Cot)
                        )
                    ),
                    onAction = onMenuAction
                )
            },
            modifier = Modifier.weight(1f)
        )

        ScientificMenuSelector(
            symbol = null,
            symbolIcon = Icons.Filled.Functions,
            title = "\u51fd\u6570",
            onAction = onAction,
            menuContent = { onMenuAction ->
                MenuGrid(
                    rows = listOf(
                        listOf(
                            MenuKey("|x|", CalculatorAction.Abs),
                            MenuKey("[x]", CalculatorAction.Floor),
                            MenuKey("\u2308x\u2309", CalculatorAction.Ceil)
                        ),
                        listOf(
                            MenuKey("rand", CalculatorAction.Rand),
                            MenuKey("\u2192dms", CalculatorAction.ToDms),
                            MenuKey("\u2192deg", CalculatorAction.ToDeg)
                        )
                    ),
                    onAction = onMenuAction
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

private data class MenuKey(
    val label: String,
    val action: CalculatorAction,
    val selected: Boolean = false,
    val closeOnClick: Boolean = true,
    val useEqualsSelectedColor: Boolean = false
)

@Composable
private fun ScientificMenuSelector(
    symbol: String?,
    symbolIcon: ImageVector? = null,
    title: String,
    onAction: (CalculatorAction) -> Unit,
    menuContent: @Composable ((CalculatorAction, Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shape = RoundedCornerShape(6.dp)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val bgNormal = MaterialTheme.colorScheme.surface
    val bgPressed = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier) {
        Surface(
            shape = shape,
            color = if (isPressed) bgPressed else bgNormal,
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (symbolIcon != null) {
                    Icon(
                        imageVector = symbolIcon,
                        contentDescription = "$title icon",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (!symbol.isNullOrBlank()) {
                    Text(
                        text = symbol,
                        color = textColor,
                        fontSize = 20.sp,
                        fontFamily = SegoeFont
                    )
                }
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 20.sp,
                    fontFamily = SegoeFont,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "$title menu",
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            menuContent { action, closeOnClick ->
                if (closeOnClick) {
                    expanded = false
                }
                onAction(action)
            }
        }
    }
}

@Composable
private fun MenuGrid(
    rows: List<List<MenuKey>>,
    onAction: (CalculatorAction, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { item ->
                    ScientificMenuKeyButton(
                        label = item.label,
                        selected = item.selected,
                        useEqualsSelectedColor = item.useEqualsSelectedColor,
                        onClick = { onAction(item.action, item.closeOnClick) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScientificMenuKeyButton(
    label: String,
    selected: Boolean,
    useEqualsSelectedColor: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(6.dp)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val equalsColor = if (isDarkTheme) KeyEqualsDark else KeyEqualsLight
    val equalsTextColor = if (isDarkTheme) Color.Black else Color.White

    val normalBg = if (selected && useEqualsSelectedColor) {
        equalsColor
    } else if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val pressedBg = if (selected && useEqualsSelectedColor) {
        equalsColor.copy(alpha = 0.85f)
    } else if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val labelColor = if (selected && useEqualsSelectedColor) {
        equalsTextColor
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = shape,
        color = if (isPressed) pressedBg else normalBg,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(vertical = 14.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                color = labelColor,
                fontFamily = FontFamily.Serif
            )
        }
    }
}
