package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.CambriaMathFont
import com.example.myapplication.ui.theme.KeyEqualsDark
import com.example.myapplication.ui.theme.KeyEqualsLight
import com.example.myapplication.ui.theme.KeyNumberDark
import com.example.myapplication.ui.theme.KeyNumberLight
import com.example.myapplication.ui.theme.KeyOtherDark
import com.example.myapplication.ui.theme.KeyOtherLight
import com.example.myapplication.ui.theme.SegoeFont

enum class CalculatorButtonStyle {
    Number,
    Function,
    Operator,
    Equals
}

@Composable
fun CalculatorButton(
    label: String,
    modifier: Modifier = Modifier,
    style: CalculatorButtonStyle = CalculatorButtonStyle.Number,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val shape = RoundedCornerShape(6.dp)
    val numberColor = if (isDarkTheme) KeyNumberDark else KeyNumberLight
    val otherColor = if (isDarkTheme) KeyOtherDark else KeyOtherLight
    val equalsColor = if (isDarkTheme) KeyEqualsDark else KeyEqualsLight

    val containerColor = when (style) {
        CalculatorButtonStyle.Number -> if (isPressed) numberColor.copy(alpha = 0.85f) else numberColor
        CalculatorButtonStyle.Function, CalculatorButtonStyle.Operator ->
            if (isPressed) otherColor.copy(alpha = 0.85f) else otherColor
        CalculatorButtonStyle.Equals -> if (isPressed) equalsColor.copy(alpha = 0.85f) else equalsColor
    }

    val textColor = when (style) {
        CalculatorButtonStyle.Equals -> if (isDarkTheme) Color.Black else Color.White
        else -> if (isDarkTheme) Color.White else Color.Black
    }

    val disabledContainerColor = when (style) {
        CalculatorButtonStyle.Equals -> equalsColor.copy(alpha = 0.45f)
        CalculatorButtonStyle.Number -> numberColor.copy(alpha = 0.45f)
        CalculatorButtonStyle.Function, CalculatorButtonStyle.Operator -> otherColor.copy(alpha = 0.45f)
    }

    val disabledTextColor = when (style) {
        CalculatorButtonStyle.Equals -> Color(160, 160, 160)
        else -> if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color(160, 160, 160)
    }

    val borderColor = when (style) {
        CalculatorButtonStyle.Equals -> equalsColor.copy(alpha = 0.75f)
        else -> if (isDarkTheme) Color.White.copy(alpha = 0.18f) else Color(220, 220, 220)
    }

    Surface(
        modifier = modifier.aspectRatio(1.67f),
        shape = shape,
        color = if (enabled) containerColor else disabledContainerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            val displayFont = if (shouldUseFormulaFont(label)) CambriaMathFont else SegoeFont
            val displayText = when (label) {
                "2nd" -> buildAnnotatedString {
                    append("2")
                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Superscript,
                            fontSize = 16.sp
                        )
                    ) {
                        append("nd")
                    }
                }
                "10\u02e3" -> buildAnnotatedString {
                    append("10")
                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift(0.65f),
                            fontSize = 16.sp
                        )
                    ) {
                        append("x")
                    }
                }
                else -> buildAnnotatedString { append(label) }
            }
            val fontSize = when (label) {
                "exp", "mod", "log", "ln", "10\u02e3" -> 22.sp
                else -> 26.sp
            }
            Text(
                text = displayText,
                fontSize = fontSize,
                color = if (enabled) textColor else disabledTextColor,
                fontFamily = displayFont
            )
        }
    }
}

private fun shouldUseFormulaFont(label: String): Boolean {
    return label.any { it == 'x' || it == 'y' || it == '\u221a' || it == '\u03c0' || it == 'e' }
}
