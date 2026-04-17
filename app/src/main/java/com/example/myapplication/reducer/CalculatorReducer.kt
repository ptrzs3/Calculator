package com.example.myapplication.reducer

import com.example.myapplication.model.CalculatorAction
import com.example.myapplication.model.CalculatorMode
import com.example.myapplication.model.CalculatorState
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private const val MAX_INPUT_DIGITS = 16

fun reduceCalculatorState(
    state: CalculatorState,
    action: CalculatorAction
): CalculatorState {
    return when (action) {
        is CalculatorAction.ChangeMode -> resetForMode(state, action.mode)
        else -> when (state.mode) {
            CalculatorMode.STANDARD -> reduceStandard(state, action)
            CalculatorMode.SCIENTIFIC -> reduceScientificMode(state, action)
            CalculatorMode.PROGRAMMER -> reduceProgrammer(state, action)
        }
    }
}

private fun resetForMode(state: CalculatorState, mode: CalculatorMode): CalculatorState {
    val base = if (mode == CalculatorMode.PROGRAMMER) state.programmerBase else 10
    return CalculatorState(
        mode = mode,
        programmerBase = base,
        wordSize = state.wordSize
    )
}

private fun reduceStandard(
    state: CalculatorState,
    action: CalculatorAction
): CalculatorState {
    return when (action) {
        is CalculatorAction.Number -> reduceNumber(state, action.value)
        is CalculatorAction.Operator -> reduceOperator(state, action.value)
        CalculatorAction.Clear -> CalculatorState(mode = state.mode, programmerBase = state.programmerBase, wordSize = state.wordSize)
        CalculatorAction.ClearEntry -> reduceClearEntry(state)
        CalculatorAction.Backspace -> reduceBackspace(state)
        CalculatorAction.ToggleSign -> reduceToggleSign(state)
        CalculatorAction.Decimal -> reduceDecimal(state)
        CalculatorAction.Percent -> reducePercent(state)
        CalculatorAction.Equals -> reduceEquals(state)
        CalculatorAction.Reciprocal -> reduceReciprocal(state)
        CalculatorAction.Square -> reduceSquare(state)
        CalculatorAction.SquareRoot -> reduceSquareRoot(state)
        else -> state
    }
}

private fun reduceScientific(
    state: CalculatorState,
    action: CalculatorAction
): CalculatorState {
    return when (action) {
        CalculatorAction.Sin -> reduceUnaryOperation(state, "sin") { sin(Math.toRadians(it)) }
        CalculatorAction.Cos -> reduceUnaryOperation(state, "cos") { cos(Math.toRadians(it)) }
        CalculatorAction.Tan -> reduceUnaryOperation(state, "tan") { tan(Math.toRadians(it)) }
        CalculatorAction.Ln -> reduceUnaryOperation(state, "ln") { if (it <= 0.0) null else ln(it) }
        CalculatorAction.Log10 -> reduceUnaryOperation(state, "log") { if (it <= 0.0) null else log10(it) }
        CalculatorAction.Pi -> replaceScientificConstant(state, Math.PI, "蟺")
        CalculatorAction.E -> replaceScientificConstant(state, Math.E, "e")
        CalculatorAction.Power -> reduceOperator(state, "^")
        else -> reduceStandard(state, action)
    }
}

private fun replaceScientificConstant(
    state: CalculatorState,
    value: Double,
    symbol: String
): CalculatorState {
    val formatted = formatResult(value)
    val newExpression = if (state.firstNumber != null && state.operator != null) {
        buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = symbol
        )
    } else {
        state.expressionText
    }

    return state.copy(
        displayText = formatted,
        expressionText = newExpression,
        isNewInput = false
    )
}

private fun reduceProgrammer(
    state: CalculatorState,
    action: CalculatorAction
): CalculatorState {
    return when (action) {
        CalculatorAction.Clear -> state.copy(
            expressionText = "",
            displayText = "0",
            programmerValue = 0L,
            firstProgrammerValue = null,
            programmerOperator = null,
            isNewInput = true
        )

        CalculatorAction.ClearEntry -> state.copy(
            displayText = "0",
            programmerValue = 0L,
            isNewInput = true,
            expressionText = buildProgrammerExpression(
                state.firstProgrammerValue,
                state.programmerOperator,
                ""
            )
        )

        CalculatorAction.Backspace -> reduceProgrammerBackspace(state)
        CalculatorAction.ToggleSign -> reduceProgrammerToggleSign(state)
        CalculatorAction.Equals -> reduceProgrammerEquals(state)
        is CalculatorAction.Number -> reduceProgrammerNumber(state, action.value)
        is CalculatorAction.Operator -> reduceProgrammerBinaryOperator(state, action.value)
        is CalculatorAction.ChangeBase -> changeProgrammerBase(state, action.base)
        is CalculatorAction.ChangeWordSize -> applyWordSize(state, action.bits)
        is CalculatorAction.ChangeProgrammerInputPane -> state.copy(programmerInputPane = action.pane)
        is CalculatorAction.ChangeProgrammerShiftMode -> state.copy(programmerShiftMode = action.mode)
        is CalculatorAction.ToggleProgrammerBit -> toggleProgrammerBit(state, action.bitIndex)
        CalculatorAction.Not -> reduceProgrammerNot(state)
        CalculatorAction.And -> reduceProgrammerBinaryOperator(state, "AND")
        CalculatorAction.Or -> reduceProgrammerBinaryOperator(state, "OR")
        CalculatorAction.Nand -> reduceProgrammerBinaryOperator(state, "NAND")
        CalculatorAction.Nor -> reduceProgrammerBinaryOperator(state, "NOR")
        CalculatorAction.Xor -> reduceProgrammerBinaryOperator(state, "XOR")
        CalculatorAction.ShiftLeft -> reduceProgrammerBinaryOperator(state, "<<")
        CalculatorAction.ShiftRight -> reduceProgrammerBinaryOperator(state, ">>")
        else -> state
    }
}

private fun reduceProgrammerNumber(state: CalculatorState, value: String): CalculatorState {
    val digitValue = value.toIntOrNull(16) ?: return state
    if (digitValue >= state.programmerBase) return state

    val currentText = if (state.isNewInput || state.displayText == "0") "" else state.displayText
    val candidate = (currentText + value.uppercase()).ifBlank { "0" }
    val parsed = parseInBase(candidate, state.programmerBase) ?: return state
    val normalized = applyWordMask(parsed, state.wordSize)
    val display = formatInBase(normalized, state.programmerBase)

    return state.copy(
        displayText = display,
        programmerValue = normalized,
        expressionText = buildProgrammerExpression(
            state.firstProgrammerValue,
            state.programmerOperator,
            display
        ),
        isNewInput = false
    )
}

private fun reduceProgrammerBackspace(state: CalculatorState): CalculatorState {
    if (state.isNewInput) return state
    val newText = state.displayText.dropLast(1).ifBlank { "0" }
    val newValue = parseInBase(newText, state.programmerBase) ?: 0L
    return state.copy(
        displayText = newText,
        programmerValue = newValue,
        expressionText = buildProgrammerExpression(
            state.firstProgrammerValue,
            state.programmerOperator,
            if (newText == "0") "" else newText
        )
    )
}

private fun reduceProgrammerToggleSign(state: CalculatorState): CalculatorState {
    val newValue = applyWordMask(-state.programmerValue, state.wordSize)
    return state.copy(
        programmerValue = newValue,
        displayText = formatInBase(newValue, state.programmerBase),
        expressionText = buildProgrammerExpression(
            state.firstProgrammerValue,
            state.programmerOperator,
            formatInBase(newValue, state.programmerBase)
        ),
        isNewInput = true
    )
}

private fun reduceProgrammerNot(state: CalculatorState): CalculatorState {
    val result = applyWordMask(state.programmerValue.inv(), state.wordSize)
    return state.copy(
        programmerValue = result,
        displayText = formatInBase(result, state.programmerBase),
        expressionText = "NOT(${formatInBase(state.programmerValue, state.programmerBase)})",
        isNewInput = true
    )
}

private fun toggleProgrammerBit(state: CalculatorState, bitIndex: Int): CalculatorState {
    if (bitIndex < 0 || bitIndex >= state.wordSize) return state

    val toggled = state.programmerValue xor (1L shl bitIndex)
    val result = applyWordMask(toggled, state.wordSize)
    return state.copy(
        programmerValue = result,
        displayText = formatInBase(result, state.programmerBase),
        expressionText = buildProgrammerExpression(
            state.firstProgrammerValue,
            state.programmerOperator,
            formatInBase(result, state.programmerBase),
            state.programmerBase
        ),
        isNewInput = false
    )
}

private fun changeProgrammerBase(state: CalculatorState, base: Int): CalculatorState {
    return state.copy(
        programmerBase = base,
        displayText = formatInBase(state.programmerValue, base),
        expressionText = buildProgrammerExpression(
            state.firstProgrammerValue,
            state.programmerOperator,
            formatInBase(state.programmerValue, base),
            base
        )
    )
}

private fun applyWordSize(state: CalculatorState, bits: Int): CalculatorState {
    val current = applyWordMask(state.programmerValue, bits)
    val first = state.firstProgrammerValue?.let { applyWordMask(it, bits) }
    return state.copy(
        wordSize = bits,
        programmerValue = current,
        firstProgrammerValue = first,
        displayText = formatInBase(current, state.programmerBase),
        expressionText = buildProgrammerExpression(
            first,
            state.programmerOperator,
            formatInBase(current, state.programmerBase),
            state.programmerBase
        )
    )
}

private fun reduceProgrammerBinaryOperator(state: CalculatorState, op: String): CalculatorState {
    val normalizedOp = normalizeProgrammerOperator(op) ?: return state
    val current = state.programmerValue

    if (state.firstProgrammerValue == null) {
        return state.copy(
            firstProgrammerValue = current,
            programmerOperator = normalizedOp,
            expressionText = buildProgrammerExpression(current, normalizedOp, "", state.programmerBase),
            isNewInput = true
        )
    }

    if (!state.isNewInput && state.programmerOperator != null) {
        val result = performProgrammerCalculation(
            first = state.firstProgrammerValue,
            operator = state.programmerOperator,
            second = current,
            wordSize = state.wordSize,
            shiftMode = state.programmerShiftMode
        ) ?: return state

        return state.copy(
            firstProgrammerValue = result,
            programmerOperator = normalizedOp,
            programmerValue = result,
            displayText = formatInBase(result, state.programmerBase),
            expressionText = buildProgrammerExpression(result, normalizedOp, "", state.programmerBase),
            isNewInput = true
        )
    }

    return state.copy(
        programmerOperator = normalizedOp,
        expressionText = buildProgrammerExpression(
            state.firstProgrammerValue,
            normalizedOp,
            "",
            state.programmerBase
        ),
        isNewInput = true
    )
}

private fun reduceProgrammerEquals(state: CalculatorState): CalculatorState {
    val first = state.firstProgrammerValue ?: return state
    val op = state.programmerOperator ?: return state
    val second = state.programmerValue
    val result = performProgrammerCalculation(first, op, second, state.wordSize, state.programmerShiftMode) ?: return state

    return state.copy(
        expressionText = buildProgrammerExpression(first, op, formatInBase(second, state.programmerBase), state.programmerBase, true),
        displayText = formatInBase(result, state.programmerBase),
        programmerValue = result,
        firstProgrammerValue = null,
        programmerOperator = null,
        isNewInput = true
    )
}

private fun performProgrammerCalculation(
    first: Long,
    operator: String,
    second: Long,
    wordSize: Int,
    shiftMode: com.example.myapplication.model.ProgrammerShiftMode
): Long? {
    val shift = second.toInt().coerceAtLeast(0)
    val normalizedOperator = normalizeProgrammerOperator(operator) ?: return null

    val raw = when (normalizedOperator) {
        "+" -> first + second
        "-" -> first - second
        "*" -> first * second
        "/" -> if (second == 0L) return null else first / second
        "mod" -> if (second == 0L) return null else first % second
        "AND" -> first and second
        "OR" -> first or second
        "NAND" -> (first and second).inv()
        "NOR" -> (first or second).inv()
        "XOR" -> first xor second
        "<<" -> when (shiftMode) {
            com.example.myapplication.model.ProgrammerShiftMode.ARITHMETIC,
            com.example.myapplication.model.ProgrammerShiftMode.LOGICAL -> first shl shift.coerceIn(0, 63)
            com.example.myapplication.model.ProgrammerShiftMode.ROTATE,
            com.example.myapplication.model.ProgrammerShiftMode.ROTATE_WITH_CARRY -> rotateLeftByWordSize(first, shift, wordSize)
        }
        ">>" -> when (shiftMode) {
            com.example.myapplication.model.ProgrammerShiftMode.ARITHMETIC -> first shr shift.coerceIn(0, 63)
            com.example.myapplication.model.ProgrammerShiftMode.LOGICAL -> logicalShiftRightByWordSize(first, shift, wordSize)
            com.example.myapplication.model.ProgrammerShiftMode.ROTATE,
            com.example.myapplication.model.ProgrammerShiftMode.ROTATE_WITH_CARRY -> rotateRightByWordSize(first, shift, wordSize)
        }
        else -> return null
    }
    return applyWordMask(raw, wordSize)
}

private fun normalizeProgrammerOperator(op: String): String? {
    return when (op.trim()) {
        "+", "-", "AND", "OR", "NAND", "NOR", "XOR", "<<", ">>" -> op.trim()
        "*", "×", "脳" -> "*"
        "/", "÷", "梅" -> "/"
        "mod", "MOD", "%" -> "mod"
        else -> null
    }
}

private fun logicalShiftRightByWordSize(value: Long, shift: Int, wordSize: Int): Long {
    if (shift <= 0) return value
    if (wordSize >= 64) return value ushr shift.coerceAtMost(63)

    val mask = (1L shl wordSize) - 1L
    val masked = value and mask
    return (masked ushr shift.coerceAtMost(63)) and mask
}

private fun rotateLeftByWordSize(value: Long, shift: Int, wordSize: Int): Long {
    val bits = wordSize.coerceIn(1, 64)
    val amount = shift % bits
    if (amount == 0) return value

    if (bits == 64) {
        return (value shl amount) or (value ushr (64 - amount))
    }

    val mask = (1L shl bits) - 1L
    val masked = value and mask
    return ((masked shl amount) or (masked ushr (bits - amount))) and mask
}

private fun rotateRightByWordSize(value: Long, shift: Int, wordSize: Int): Long {
    val bits = wordSize.coerceIn(1, 64)
    val amount = shift % bits
    if (amount == 0) return value

    if (bits == 64) {
        return (value ushr amount) or (value shl (64 - amount))
    }

    val mask = (1L shl bits) - 1L
    val masked = value and mask
    return ((masked ushr amount) or (masked shl (bits - amount))) and mask
}

private fun applyWordMask(value: Long, wordSize: Int): Long {
    return when (wordSize) {
        8 -> value.toByte().toLong()
        16 -> value.toShort().toLong()
        32 -> value.toInt().toLong()
        else -> value
    }
}

private fun buildProgrammerExpression(
    first: Long?,
    operator: String?,
    currentInput: String,
    base: Int = 10,
    includeEquals: Boolean = false
): String {
    if (first == null || operator == null) return ""
    val left = formatInBase(first, base)
    val right = currentInput.ifBlank { "" }
    return when {
        right.isBlank() && !includeEquals -> "$left $operator"
        right.isNotBlank() && !includeEquals -> "$left $operator $right"
        right.isNotBlank() && includeEquals -> "$left $operator $right ="
        else -> "$left $operator ="
    }
}

private fun parseInBase(text: String, base: Int): Long? {
    return text.toLongOrNull(base)
}

private fun formatInBase(value: Long, base: Int): String {
    return when (base) {
        2 -> value.toULong().toString(2)
        8 -> value.toULong().toString(8)
        10 -> value.toString()
        16 -> value.toULong().toString(16).uppercase()
        else -> value.toString()
    }
}

private fun reduceUnaryOperation(
    state: CalculatorState,
    operationLabel: String,
    transform: (Double) -> Double?
): CalculatorState {
    val currentNumber = state.displayText.toDoubleOrNull() ?: return state

    if (state.displayText == "Error") {
        return state
    }

    val result = transform(currentNumber) ?: return state.copy(
        expressionText = "$operationLabel(${state.displayText})",
        displayText = "Error",
        firstNumber = null,
        operator = null,
        isNewInput = true
    )

    val formatted = formatResult(result)

    val newExpression = if (state.firstNumber != null && state.operator != null) {
        buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = formatted
        )
    } else {
        "$operationLabel(${state.displayText})"
    }

    return state.copy(
        expressionText = newExpression,
        displayText = formatted,
        isNewInput = true
    )
}

private fun reduceReciprocal(state: CalculatorState): CalculatorState {
    val currentNumber = state.displayText.toDoubleOrNull() ?: return state
    if (state.displayText == "Error") return state

    if (currentNumber == 0.0) {
        return state.copy(
            expressionText = "1/(${state.displayText})",
            displayText = "Error",
            firstNumber = null,
            operator = null,
            isNewInput = true
        )
    }

    val result = 1.0 / currentNumber
    val formatted = formatResult(result)

    val newExpression = if (state.firstNumber != null && state.operator != null) {
        buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = formatted
        )
    } else {
        "1/(${state.displayText})"
    }

    return state.copy(
        expressionText = newExpression,
        displayText = formatted,
        isNewInput = true
    )
}

private fun reduceSquare(state: CalculatorState): CalculatorState {
    val currentNumber = state.displayText.toDoubleOrNull() ?: return state
    if (state.displayText == "Error") return state

    val result = currentNumber * currentNumber
    val formatted = formatResult(result)

    val newExpression = if (state.firstNumber != null && state.operator != null) {
        buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = formatted
        )
    } else {
        "sqr(${state.displayText})"
    }

    return state.copy(
        expressionText = newExpression,
        displayText = formatted,
        isNewInput = true
    )
}

private fun reduceSquareRoot(state: CalculatorState): CalculatorState {
    val currentNumber = state.displayText.toDoubleOrNull() ?: return state
    if (state.displayText == "Error") return state

    if (currentNumber < 0.0) {
        return state.copy(
            expressionText = "鈭?${state.displayText})",
            displayText = "Error",
            firstNumber = null,
            operator = null,
            isNewInput = true
        )
    }

    val result = sqrt(currentNumber)
    val formatted = formatResult(result)

    val newExpression = if (state.firstNumber != null && state.operator != null) {
        buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = formatted
        )
    } else {
        "鈭?${state.displayText})"
    }

    return state.copy(
        expressionText = newExpression,
        displayText = formatted,
        isNewInput = true
    )
}

private fun reduceClearEntry(state: CalculatorState): CalculatorState {
    return if (state.firstNumber != null && state.operator != null) {
        state.copy(
            displayText = "0",
            expressionText = buildExpressionText(
                firstNumber = state.firstNumber,
                operator = state.operator,
                currentInput = ""
            ),
            isNewInput = true
        )
    } else {
        state.copy(
            displayText = "0",
            expressionText = "",
            isNewInput = true
        )
    }
}

private fun reducePercent(state: CalculatorState): CalculatorState {
    val currentNumber = state.displayText.toDoubleOrNull()

    if (currentNumber == null || state.displayText == "Error") {
        return state
    }

    if (state.firstNumber == null || state.operator == null) {
        val result = currentNumber / 100.0
        return state.copy(
            displayText = formatResult(result),
            expressionText = "",
            isNewInput = true
        )
    }

    val percentValue = when (state.operator) {
        "+", "-" -> state.firstNumber * currentNumber / 100.0
        "脳", "梅" -> currentNumber / 100.0
        else -> return state
    }

    val formatted = formatResult(percentValue)

    return state.copy(
        displayText = formatted,
        expressionText = buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = formatted
        ),
        isNewInput = true
    )
}

private fun reduceNumber(state: CalculatorState, value: String): CalculatorState {
    val newDisplay = if (
        state.isNewInput ||
        state.displayText == "0" ||
        state.displayText == "Error"
    ) {
        value
    } else {
        state.displayText + value
    }

    if (newDisplay.countDigits() > MAX_INPUT_DIGITS) {
        return state
    }

    val newExpression = if (state.firstNumber != null && state.operator != null) {
        buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = newDisplay
        )
    } else {
        state.expressionText
    }

    return state.copy(
        displayText = newDisplay,
        expressionText = newExpression,
        isNewInput = false
    )
}

private fun String.countDigits(): Int = count { it.isDigit() }

private fun reduceOperator(state: CalculatorState, op: String): CalculatorState {
    val currentNumber = state.displayText.toDoubleOrNull()

    if (state.displayText == "Error" || currentNumber == null) {
        return state
    }

    if (state.firstNumber == null) {
        return state.copy(
            firstNumber = currentNumber,
            operator = op,
            expressionText = buildExpressionText(
                firstNumber = currentNumber,
                operator = op,
                currentInput = ""
            ),
            isNewInput = true
        )
    }

    if (!state.isNewInput && state.operator != null) {
        val result = performCalculation(
            first = state.firstNumber,
            operator = state.operator,
            second = currentNumber
        ) ?: return state

        if (result == "Error") {
            return state.copy(
                expressionText = "",
                displayText = "Error",
                firstNumber = null,
                operator = null,
                isNewInput = true
            )
        }

        val numericResult = result.toDouble()
        val formatted = formatResult(numericResult)

        return state.copy(
            expressionText = buildExpressionText(
                firstNumber = numericResult,
                operator = op,
                currentInput = ""
            ),
            displayText = formatted,
            firstNumber = numericResult,
            operator = op,
            isNewInput = true
        )
    }

    return state.copy(
        operator = op,
        expressionText = buildExpressionText(
            firstNumber = state.firstNumber,
            operator = op,
            currentInput = ""
        ),
        isNewInput = true
    )
}

private fun reduceEquals(state: CalculatorState): CalculatorState {
    val secondNumber = state.displayText.toDoubleOrNull()

    if (state.firstNumber == null || state.operator == null || secondNumber == null) {
        return state
    }

    val expression = buildExpressionText(
        firstNumber = state.firstNumber,
        operator = state.operator,
        currentInput = formatResult(secondNumber),
        includeEquals = true
    )

    val result = performCalculation(
        first = state.firstNumber,
        operator = state.operator,
        second = secondNumber
    ) ?: return state

    if (result == "Error") {
        return state.copy(
            expressionText = expression,
            displayText = "Error",
            firstNumber = null,
            operator = null,
            isNewInput = true
        )
    }

    val numericResult = result.toDouble()
    val formatted = formatResult(numericResult)

    return state.copy(
        expressionText = expression,
        displayText = formatted,
        firstNumber = null,
        operator = null,
        isNewInput = true
    )
}

private fun reduceBackspace(state: CalculatorState): CalculatorState {
    if (state.isNewInput) {
        return state
    }

    val text = state.displayText

    val newText = when {
        text == "Error" -> "0"
        text.length <= 1 -> "0"
        text.startsWith("-") && text.length == 2 -> "0"
        else -> text.dropLast(1)
    }

    val newExpression = if (state.firstNumber != null && state.operator != null) {
        buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = if (newText == "0") "" else newText
        )
    } else {
        state.expressionText
    }

    return state.copy(
        displayText = newText,
        expressionText = newExpression
    )
}

private fun reduceToggleSign(state: CalculatorState): CalculatorState {
    val text = state.displayText

    val newText = when {
        text == "0" || text == "Error" -> "0"
        text.startsWith("-") -> text.removePrefix("-")
        else -> "-$text"
    }

    val newExpression = if (state.firstNumber != null && state.operator != null) {
        buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = newText
        )
    } else {
        state.expressionText
    }

    return state.copy(
        displayText = newText,
        expressionText = newExpression
    )
}

private fun reduceDecimal(state: CalculatorState): CalculatorState {
    val text = state.displayText

    val newText = when {
        text == "Error" -> "0."
        state.isNewInput -> "0."
        text.contains(".") -> text
        else -> "$text."
    }

    val newExpression = if (state.firstNumber != null && state.operator != null) {
        buildExpressionText(
            firstNumber = state.firstNumber,
            operator = state.operator,
            currentInput = newText
        )
    } else {
        state.expressionText
    }

    return state.copy(
        displayText = newText,
        expressionText = newExpression,
        isNewInput = false
    )
}

private fun buildExpressionText(
    firstNumber: Double?,
    operator: String?,
    currentInput: String,
    includeEquals: Boolean = false
): String {
    if (firstNumber == null || operator == null) {
        return ""
    }

    val left = formatResult(firstNumber)
    val right = if (currentInput.isBlank() || currentInput == "Error") {
        ""
    } else {
        currentInput
    }

    return when {
        right.isBlank() && !includeEquals -> "$left $operator"
        right.isNotBlank() && !includeEquals -> "$left $operator $right"
        right.isNotBlank() && includeEquals -> "$left $operator $right ="
        else -> "$left $operator ="
    }
}

private fun performCalculation(first: Double, operator: String, second: Double): String? {
    val result = when (operator) {
        "+" -> first + second
        "-" -> first - second
        "脳" -> first * second
        "梅" -> {
            if (second == 0.0) {
                return "Error"
            } else {
                first / second
            }
        }
        "^" -> Math.pow(first, second)
        else -> return null
    }

    return result.toString()
}

private fun formatResult(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "Error"
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}
