package com.example.myapplication.reducer

import com.example.myapplication.model.AdcDirection
import com.example.myapplication.model.AdcEncoding
import com.example.myapplication.model.CalculatorAction
import com.example.myapplication.model.CalculatorState
import kotlin.math.roundToLong

fun reduceAdcMode(state: CalculatorState, action: CalculatorAction): CalculatorState {
    return when (action) {
        CalculatorAction.Clear -> resetAdc(state)
        CalculatorAction.ClearEntry -> reduceAdcClearEntry(state)
        CalculatorAction.Backspace -> reduceAdcBackspace(state)
        CalculatorAction.ToggleSign -> reduceAdcToggleSign(state)
        CalculatorAction.Decimal -> reduceAdcDecimal(state)
        is CalculatorAction.Number -> reduceAdcNumber(state, action.value)
        CalculatorAction.ToggleAdcDirection -> toggleAdcDirection(state)
        is CalculatorAction.ChangeAdcResolution -> changeAdcResolution(state, action.bits)
        is CalculatorAction.ChangeAdcVrefPlus -> changeAdcVrefPlus(state, action.voltage)
        is CalculatorAction.ChangeAdcVrefMinus -> changeAdcVrefMinus(state, action.voltage)
        is CalculatorAction.ChangeAdcEncoding -> changeAdcEncoding(state, action.encoding)
        CalculatorAction.ApplyAdcPreset -> cycleAdcPreset(state)
        else -> state
    }
}

private fun resetAdc(state: CalculatorState): CalculatorState = state.copy(
    expressionText = "",
    displayText = "0",
    adcDigitalValue = 0L,
    adcAnalogValue = 0.0,
    isNewInput = true
)

private fun reduceAdcClearEntry(state: CalculatorState): CalculatorState = state.copy(
    displayText = "0",
    adcDigitalValue = 0L,
    adcAnalogValue = 0.0,
    isNewInput = true,
    expressionText = ""
)

private fun reduceAdcBackspace(state: CalculatorState): CalculatorState {
    if (state.isNewInput) return state
    val newText = state.displayText.dropLast(1).ifBlank { "0" }
    return updateAdcWithInput(state, newText)
}

private fun reduceAdcToggleSign(state: CalculatorState): CalculatorState {
    if (state.adcDirection != AdcDirection.ANALOG_TO_DIGITAL) return state
    val text = state.displayText
    val newText = when {
        text == "0" || text == "Error" -> "0"
        text.startsWith("-") -> text.removePrefix("-")
        else -> "-$text"
    }
    return updateAdcWithInput(state, newText)
}

private fun reduceAdcDecimal(state: CalculatorState): CalculatorState {
    if (state.adcDirection != AdcDirection.ANALOG_TO_DIGITAL) return state
    val text = state.displayText
    val newText = when {
        text == "Error" -> "0."
        state.isNewInput -> "0."
        text.contains(".") -> text
        else -> "$text."
    }
    return state.copy(displayText = newText, isNewInput = false)
}

private fun reduceAdcNumber(state: CalculatorState, value: String): CalculatorState {
    return when (state.adcDirection) {
        AdcDirection.DIGITAL_TO_ANALOG -> reduceAdcDigitalNumber(state, value)
        AdcDirection.ANALOG_TO_DIGITAL -> reduceAdcAnalogNumber(state, value)
    }
}

private fun reduceAdcDigitalNumber(state: CalculatorState, value: String): CalculatorState {
    val digitValue = value.toIntOrNull(16) ?: return state

    val currentText = if (state.isNewInput || state.displayText == "0") "" else state.displayText
    val candidate = (currentText + value.uppercase())
    if (candidate.isBlank()) return state

    val maxHexDigits = ((state.adcResolution + 3) / 4).coerceAtLeast(1)
    if (candidate.length > maxHexDigits) return state

    val parsed = candidate.toLongOrNull(16) ?: return state
    val maxCode = (1L shl state.adcResolution) - 1L
    if (parsed > maxCode) return state

    val newAnalog = codeToVoltage(parsed, state.adcVrefPlus, state.adcVrefMinus,
        state.adcResolution, state.adcEncoding)
    val expression = when (state.adcEncoding) {
        AdcEncoding.STRAIGHT_BINARY -> "0x${candidate} = ${formatVoltage(newAnalog)}"
        AdcEncoding.TWOS_COMPLEMENT -> "0x${candidate} = ${formatVoltage(newAnalog)}"
    }

    return state.copy(
        displayText = candidate,
        adcDigitalValue = parsed,
        adcAnalogValue = newAnalog,
        expressionText = expression,
        isNewInput = false
    )
}

private fun reduceAdcAnalogNumber(state: CalculatorState, value: String): CalculatorState {
    val newDisplay = if (state.isNewInput || state.displayText == "0") {
        value
    } else {
        state.displayText + value
    }

    val digitCount = newDisplay.count { it.isDigit() }
    if (digitCount > 12) return state

    return updateAdcWithInput(state, newDisplay)
}

private fun updateAdcWithInput(state: CalculatorState, inputText: String): CalculatorState {
    return when (state.adcDirection) {
        AdcDirection.DIGITAL_TO_ANALOG -> {
            val parsed = inputText.toLongOrNull(16) ?: run {
                return state.copy(displayText = inputText, isNewInput = false)
            }
            val maxCode = (1L shl state.adcResolution) - 1L
            if (parsed > maxCode) return state
            val newAnalog = codeToVoltage(parsed, state.adcVrefPlus, state.adcVrefMinus,
                state.adcResolution, state.adcEncoding)
            val expression = "0x${inputText.uppercase()} = ${formatVoltage(newAnalog)}"
            state.copy(
                displayText = inputText.uppercase(),
                adcDigitalValue = parsed,
                adcAnalogValue = newAnalog,
                expressionText = expression,
                isNewInput = false
            )
        }
        AdcDirection.ANALOG_TO_DIGITAL -> {
            val voltage = inputText.toDoubleOrNull() ?: run {
                return state.copy(displayText = inputText, isNewInput = false)
            }
            val newCode = voltageToCode(voltage, state.adcVrefPlus, state.adcVrefMinus,
                state.adcResolution, state.adcEncoding)
            val codeHex = newCode.toString(16).uppercase().padStart(
                ((state.adcResolution + 3) / 4).coerceAtLeast(1), '0'
            )
            val expression = "${formatVoltage(voltage)} = 0x$codeHex"
            state.copy(
                displayText = inputText,
                adcAnalogValue = voltage,
                adcDigitalValue = newCode,
                expressionText = expression,
                isNewInput = false
            )
        }
    }
}

private fun toggleAdcDirection(state: CalculatorState): CalculatorState {
    val newDirection = when (state.adcDirection) {
        AdcDirection.DIGITAL_TO_ANALOG -> AdcDirection.ANALOG_TO_DIGITAL
        AdcDirection.ANALOG_TO_DIGITAL -> AdcDirection.DIGITAL_TO_ANALOG
    }

    val newDisplay = when (newDirection) {
        AdcDirection.DIGITAL_TO_ANALOG -> {
            val codeHex = state.adcDigitalValue.toString(16).uppercase()
            if (codeHex == "0") "0" else codeHex
        }
        AdcDirection.ANALOG_TO_DIGITAL -> formatVoltage(state.adcAnalogValue)
    }

    val newExpression = when (newDirection) {
        AdcDirection.DIGITAL_TO_ANALOG -> {
            val codeHex = state.adcDigitalValue.toString(16).uppercase()
            "0x$codeHex = ${formatVoltage(state.adcAnalogValue)}"
        }
        AdcDirection.ANALOG_TO_DIGITAL -> {
            val codeHex = state.adcDigitalValue.toString(16).uppercase().padStart(
                ((state.adcResolution + 3) / 4).coerceAtLeast(1), '0'
            )
            "${formatVoltage(state.adcAnalogValue)} = 0x$codeHex"
        }
    }

    return state.copy(
        adcDirection = newDirection,
        displayText = newDisplay,
        expressionText = newExpression,
        isNewInput = true
    )
}

private fun changeAdcResolution(state: CalculatorState, bits: Int): CalculatorState {
    val clamped = bits.coerceIn(1, 24)
    val digital = state.adcDigitalValue and ((1L shl clamped) - 1L)
    val analog = codeToVoltage(digital, state.adcVrefPlus, state.adcVrefMinus, clamped, state.adcEncoding)
    val codeHex = digital.toString(16).uppercase()

    val expression = when (state.adcDirection) {
        AdcDirection.DIGITAL_TO_ANALOG -> "0x$codeHex = ${formatVoltage(analog)}"
        AdcDirection.ANALOG_TO_DIGITAL -> {
            val paddedHex = codeHex.padStart(((clamped + 3) / 4).coerceAtLeast(1), '0')
            "${formatVoltage(analog)} = 0x$paddedHex"
        }
    }

    val display = when (state.adcDirection) {
        AdcDirection.DIGITAL_TO_ANALOG -> if (codeHex == "0") "0" else codeHex
        AdcDirection.ANALOG_TO_DIGITAL -> formatVoltage(analog)
    }

    return state.copy(
        adcResolution = clamped,
        adcDigitalValue = digital,
        adcAnalogValue = analog,
        displayText = display,
        expressionText = expression,
        isNewInput = true
    )
}

private fun changeAdcVrefPlus(state: CalculatorState, voltage: Double): CalculatorState {
    val analog = codeToVoltage(state.adcDigitalValue, voltage, state.adcVrefMinus,
        state.adcResolution, state.adcEncoding)
    val codeHex = state.adcDigitalValue.toString(16).uppercase()

    return state.copy(
        adcVrefPlus = voltage,
        adcAnalogValue = analog,
        displayText = when (state.adcDirection) {
            AdcDirection.DIGITAL_TO_ANALOG -> if (codeHex == "0") "0" else codeHex
            AdcDirection.ANALOG_TO_DIGITAL -> formatVoltage(analog)
        },
        expressionText = when (state.adcDirection) {
            AdcDirection.DIGITAL_TO_ANALOG -> "0x$codeHex = ${formatVoltage(analog)}"
            AdcDirection.ANALOG_TO_DIGITAL -> "${formatVoltage(analog)} = 0x$codeHex"
        },
        isNewInput = true
    )
}

private fun changeAdcVrefMinus(state: CalculatorState, voltage: Double): CalculatorState {
    val analog = codeToVoltage(state.adcDigitalValue, state.adcVrefPlus, voltage,
        state.adcResolution, state.adcEncoding)
    val codeHex = state.adcDigitalValue.toString(16).uppercase()

    return state.copy(
        adcVrefMinus = voltage,
        adcAnalogValue = analog,
        displayText = when (state.adcDirection) {
            AdcDirection.DIGITAL_TO_ANALOG -> if (codeHex == "0") "0" else codeHex
            AdcDirection.ANALOG_TO_DIGITAL -> formatVoltage(analog)
        },
        expressionText = when (state.adcDirection) {
            AdcDirection.DIGITAL_TO_ANALOG -> "0x$codeHex = ${formatVoltage(analog)}"
            AdcDirection.ANALOG_TO_DIGITAL -> "${formatVoltage(analog)} = 0x$codeHex"
        },
        isNewInput = true
    )
}

private fun changeAdcEncoding(state: CalculatorState, encoding: AdcEncoding): CalculatorState {
    val analog = codeToVoltage(state.adcDigitalValue, state.adcVrefPlus, state.adcVrefMinus,
        state.adcResolution, encoding)
    val codeHex = state.adcDigitalValue.toString(16).uppercase()

    return state.copy(
        adcEncoding = encoding,
        adcAnalogValue = analog,
        displayText = when (state.adcDirection) {
            AdcDirection.DIGITAL_TO_ANALOG -> if (codeHex == "0") "0" else codeHex
            AdcDirection.ANALOG_TO_DIGITAL -> formatVoltage(analog)
        },
        expressionText = when (state.adcDirection) {
            AdcDirection.DIGITAL_TO_ANALOG -> "0x$codeHex = ${formatVoltage(analog)}"
            AdcDirection.ANALOG_TO_DIGITAL -> "${formatVoltage(analog)} = 0x$codeHex"
        },
        isNewInput = true
    )
}

fun cycleAdcPreset(state: CalculatorState): CalculatorState {
    val nextIndex = (state.adcPresetIndex + 1) % adcPresets.size
    val preset = adcPresets[nextIndex]
    val newResolution = preset.resolution
    val mask = (1L shl newResolution) - 1L
    val digital = state.adcDigitalValue and mask
    val analog = codeToVoltage(digital, preset.vrefPlus, preset.vrefMinus, newResolution, preset.encoding)
    val codeHex = digital.toString(16).uppercase()

    return state.copy(
        adcPresetIndex = nextIndex,
        adcResolution = newResolution,
        adcVrefPlus = preset.vrefPlus,
        adcVrefMinus = preset.vrefMinus,
        adcEncoding = preset.encoding,
        adcDigitalValue = digital,
        adcAnalogValue = analog,
        displayText = when (state.adcDirection) {
            AdcDirection.DIGITAL_TO_ANALOG -> if (codeHex == "0") "0" else codeHex
            AdcDirection.ANALOG_TO_DIGITAL -> formatVoltage(analog)
        },
        expressionText = when (state.adcDirection) {
            AdcDirection.DIGITAL_TO_ANALOG -> "0x$codeHex = ${formatVoltage(analog)}"
            AdcDirection.ANALOG_TO_DIGITAL -> "${formatVoltage(analog)} = 0x$codeHex"
        },
        isNewInput = true
    )
}

// --- Core conversion functions ---

internal fun maxCode(resolution: Int): Long = (1L shl resolution) - 1L

internal fun lsbVoltage(vrefPlus: Double, vrefMinus: Double, resolution: Int): Double {
    val max = maxCode(resolution)
    if (max == 0L) return 0.0
    return (vrefPlus - vrefMinus) / max.toDouble()
}

internal fun codeToVoltage(
    code: Long,
    vrefPlus: Double,
    vrefMinus: Double,
    resolution: Int,
    encoding: AdcEncoding
): Double {
    val max = maxCode(resolution)
    if (max == 0L) return 0.0
    val codeDouble = when (encoding) {
        AdcEncoding.STRAIGHT_BINARY -> (code and max).toDouble()
        AdcEncoding.TWOS_COMPLEMENT -> signExtend(code, resolution).toDouble()
    }
    return vrefMinus + (codeDouble / max.toDouble()) * (vrefPlus - vrefMinus)
}

internal fun voltageToCode(
    voltage: Double,
    vrefPlus: Double,
    vrefMinus: Double,
    resolution: Int,
    encoding: AdcEncoding
): Long {
    val max = maxCode(resolution)
    if (max == 0L) return 0L

    return when (encoding) {
        AdcEncoding.STRAIGHT_BINARY -> {
            val raw = ((voltage - vrefMinus) / (vrefPlus - vrefMinus) * max.toDouble())
            raw.roundToLong().coerceIn(0L, max)
        }
        AdcEncoding.TWOS_COMPLEMENT -> {
            val halfMax = (max + 1) / 2
            val raw = ((voltage - vrefMinus) / (vrefPlus - vrefMinus) * max.toDouble())
            val signed = (raw - halfMax.toDouble()).roundToLong()
            signed.coerceIn(-halfMax, halfMax - 1) and max
        }
    }
}

internal fun signExtend(code: Long, resolution: Int): Long {
    if (resolution >= 64) return code
    val signBit = 1L shl (resolution - 1)
    return if ((code and signBit) != 0L) {
        code or (-1L shl resolution)
    } else {
        code
    }
}

internal fun formatVoltage(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    val absVal = kotlin.math.abs(value)
    return when {
        absVal == 0.0 -> "0"
        absVal >= 100.0 -> String.format("%.1f", value)
        absVal >= 10.0 -> String.format("%.2f", value)
        absVal >= 1.0 -> String.format("%.3f", value)
        absVal >= 0.001 -> String.format("%.4f", value)
        else -> String.format("%.6f", value)
    }
}

internal val adcPresets = listOf(
    AdcPreset("12-bit 3.3V", 12, 3.3, 0.0, AdcEncoding.STRAIGHT_BINARY),
    AdcPreset("16-bit 5V", 16, 5.0, 0.0, AdcEncoding.STRAIGHT_BINARY),
    AdcPreset("10-bit 3.3V", 10, 3.3, 0.0, AdcEncoding.STRAIGHT_BINARY),
    AdcPreset("24-bit 3.3V", 24, 3.3, 0.0, AdcEncoding.STRAIGHT_BINARY),
    AdcPreset("8-bit 5V", 8, 5.0, 0.0, AdcEncoding.STRAIGHT_BINARY),
    AdcPreset("12-bit +/-5V", 12, 5.0, -5.0, AdcEncoding.TWOS_COMPLEMENT),
    AdcPreset("16-bit +/-10V", 16, 10.0, -10.0, AdcEncoding.TWOS_COMPLEMENT),
    AdcPreset("14-bit 3.3V", 14, 3.3, 0.0, AdcEncoding.STRAIGHT_BINARY),
    AdcPreset("20-bit 5V", 20, 5.0, 0.0, AdcEncoding.STRAIGHT_BINARY)
)

data class AdcPreset(
    val name: String,
    val resolution: Int,
    val vrefPlus: Double,
    val vrefMinus: Double,
    val encoding: AdcEncoding
)
