package com.example.myapplication.ui

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.android.awaitFrame
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun Display(
    expressionText: String,
    displayText: String,
    preferTrailingEdge: Boolean,
    fontFamily: FontFamily? = null
) {
    val formattedDisplay = formatDisplayNumber(displayText)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        if (expressionText.isNotBlank()) {
            Text(
                text = expressionText,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                maxLines = 1,
                fontFamily = fontFamily,
                modifier = Modifier.fillMaxWidth()
            )
        }

        AutoFitSingleLineText(
            text = formattedDisplay,
            copyText = displayText,
            maxFontSize = 48.sp,
            minFontSize = 34.sp,
            stepGranularity = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            fontFamily = fontFamily,
            preferTrailingEdge = preferTrailingEdge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}

@Composable
private fun AutoFitSingleLineText(
    text: String,
    copyText: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    stepGranularity: TextUnit = 1.sp,
    color: Color = Color.Black,
    textAlign: TextAlign = TextAlign.End,
    fontFamily: FontFamily? = null,
    preferTrailingEdge: Boolean
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier) {
        val availableWidthDp = maxWidth
        val availableWidthPx = with(density) { maxWidth.roundToPx() }

        val bestFontSize = remember(
            text,
            availableWidthPx,
            maxFontSize,
            minFontSize,
            stepGranularity,
            fontFamily
        ) {
            findBestFontSize(
                text = text,
                availableWidthPx = availableWidthPx,
                maxFontSize = maxFontSize,
                minFontSize = minFontSize,
                stepGranularity = stepGranularity,
                textMeasurer = textMeasurer,
                fontFamily = fontFamily
            )
        }

        LaunchedEffect(text, bestFontSize, preferTrailingEdge) {
            awaitFrame()
            if (preferTrailingEdge) {
                scrollState.scrollTo(scrollState.maxValue)
            } else {
                scrollState.scrollTo(0)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(copyText))
                        Toast.makeText(context, "\u5df2\u590d\u5236\u5230\u526a\u8d34\u677f", Toast.LENGTH_SHORT).show()
                    }
                )
                .horizontalScroll(scrollState),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                fontSize = bestFontSize,
                color = color,
                textAlign = textAlign,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                fontFamily = fontFamily,
                modifier = Modifier.widthIn(min = availableWidthDp)
            )
        }
    }
}

private fun findBestFontSize(
    text: String,
    availableWidthPx: Int,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    stepGranularity: TextUnit,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    fontFamily: FontFamily?
): TextUnit {
    if (text.isBlank() || availableWidthPx <= 0) return maxFontSize

    var low = minFontSize.value
    var high = maxFontSize.value
    var best = minFontSize.value
    val step = stepGranularity.value.coerceAtLeast(0.1f)

    while (high - low > step) {
        val mid = (low + high) / 2f
        val candidate = mid.sp

        val fits = textFitsWidth(
            text = text,
            fontSize = candidate,
            availableWidthPx = availableWidthPx,
            textMeasurer = textMeasurer,
            fontFamily = fontFamily
        )

        if (fits) {
            best = mid
            low = mid
        } else {
            high = mid
        }
    }

    return best.sp
}

private fun textFitsWidth(
    text: String,
    fontSize: TextUnit,
    availableWidthPx: Int,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    fontFamily: FontFamily?
): Boolean {
    return measureTextWidth(
        text = text,
        fontSize = fontSize,
        textMeasurer = textMeasurer,
        fontFamily = fontFamily
    ) <= availableWidthPx
}

private fun measureTextWidth(
    text: String,
    fontSize: TextUnit,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    fontFamily: FontFamily?
): Int {
    val result = textMeasurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(
            fontSize = fontSize,
            fontFamily = fontFamily
        ),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
        constraints = Constraints()
    )
    return result.size.width
}

private fun formatDisplayNumber(raw: String): String {
    if (raw.isBlank()) return "0"
    if (raw == "Error") return raw

    val negative = raw.startsWith("-")
    val absRaw = if (negative) raw.removePrefix("-") else raw

    val endsWithDot = absRaw.endsWith(".")
    val parts = absRaw.split(".", limit = 2)
    val integerPartRaw = parts.getOrElse(0) { "0" }
    val fractionPartRaw = parts.getOrNull(1)

    val integerFormatted = formatIntegerPart(integerPartRaw)

    val result = when {
        endsWithDot -> "$integerFormatted."
        fractionPartRaw != null -> "$integerFormatted.$fractionPartRaw"
        else -> integerFormatted
    }

    return if (negative && result != "0") "-$result" else result
}

private fun formatIntegerPart(integerPartRaw: String): String {
    val safe = integerPartRaw.ifBlank { "0" }

    if (!safe.all { it.isDigit() }) return safe

    return try {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        formatter.format(BigDecimal(safe))
    } catch (_: Exception) {
        safe
    }
}
