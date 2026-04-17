package com.example.myapplication.reducer

import com.example.myapplication.model.CalculatorAction
import com.example.myapplication.model.CalculatorState
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh
import kotlin.math.cosh
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.random.Random

private const val SCIENTIFIC_MAX_INPUT_DIGITS = 16

fun reduceScientificMode(
    state: CalculatorState,
    action: CalculatorAction
): CalculatorState {
    return when (action) {
        CalculatorAction.Clear -> state.copy(
            expressionText = "",
            displayText = "0",
            firstNumber = null,
            operator = null,
            isNewInput = true,
            scientificExpression = ""
        )
        CalculatorAction.ClearEntry -> state.copy(
            displayText = "0",
            isNewInput = true
        )
        CalculatorAction.Backspace -> reduceScientificBackspace(state)
        CalculatorAction.ToggleSign -> reduceScientificToggleSign(state)
        CalculatorAction.Decimal -> reduceScientificDecimal(state)
        is CalculatorAction.Number -> reduceScientificNumber(state, action.value)
        is CalculatorAction.Operator -> reduceScientificOperator(state, action.value)
        CalculatorAction.Power -> reduceScientificOperator(state, "^")
        CalculatorAction.OpenParenthesis -> reduceScientificOpenParenthesis(state)
        CalculatorAction.CloseParenthesis -> reduceScientificCloseParenthesis(state)
        CalculatorAction.Equals -> reduceScientificEquals(state)
        CalculatorAction.Sin -> if (state.scientificTrigSecondEnabled && state.scientificHypEnabled) {
            reduceScientificUnary(state, "sinh^-1") { asinh(it) }
        } else if (state.scientificTrigSecondEnabled) {
            reduceScientificUnary(state, "sin^-1") {
                if (it < -1.0 || it > 1.0) null else Math.toDegrees(asin(it))
            }
        } else if (state.scientificHypEnabled) {
            reduceScientificUnary(state, "sinh") { sinh(it) }
        } else {
            reduceScientificUnary(state, "sin") { sin(Math.toRadians(it)) }
        }
        CalculatorAction.Cos -> if (state.scientificTrigSecondEnabled && state.scientificHypEnabled) {
            reduceScientificUnary(state, "cosh^-1") { acosh(it) }
        } else if (state.scientificTrigSecondEnabled) {
            reduceScientificUnary(state, "cos^-1") {
                if (it < -1.0 || it > 1.0) null else Math.toDegrees(acos(it))
            }
        } else if (state.scientificHypEnabled) {
            reduceScientificUnary(state, "cosh") { cosh(it) }
        } else {
            reduceScientificUnary(state, "cos") { cos(Math.toRadians(it)) }
        }
        CalculatorAction.Tan -> if (state.scientificTrigSecondEnabled && state.scientificHypEnabled) {
            reduceScientificUnary(state, "tanh^-1") { atanh(it) }
        } else if (state.scientificTrigSecondEnabled) {
            reduceScientificUnary(state, "tan^-1") { Math.toDegrees(atan(it)) }
        } else if (state.scientificHypEnabled) {
            reduceScientificUnary(state, "tanh") { tanh(it) }
        } else {
            reduceScientificUnary(state, "tan") { tan(Math.toRadians(it)) }
        }
        CalculatorAction.Sec -> if (state.scientificTrigSecondEnabled && state.scientificHypEnabled) {
            reduceScientificUnary(state, "sech^-1") { asech(it) }
        } else if (state.scientificTrigSecondEnabled) {
            reduceScientificUnary(state, "sec^-1") {
                if (abs(it) < 1.0) null else Math.toDegrees(acos(1.0 / it))
            }
        } else if (state.scientificHypEnabled) {
            reduceScientificUnary(state, "sech") {
                val denominator = cosh(it)
                if (denominator == 0.0) null else 1.0 / denominator
            }
        } else {
            reduceScientificUnary(state, "sec") {
                val denominator = cos(Math.toRadians(it))
                if (denominator == 0.0) null else 1.0 / denominator
            }
        }
        CalculatorAction.Csc -> if (state.scientificTrigSecondEnabled && state.scientificHypEnabled) {
            reduceScientificUnary(state, "csch^-1") { acsch(it) }
        } else if (state.scientificTrigSecondEnabled) {
            reduceScientificUnary(state, "csc^-1") {
                if (abs(it) < 1.0) null else Math.toDegrees(asin(1.0 / it))
            }
        } else if (state.scientificHypEnabled) {
            reduceScientificUnary(state, "csch") {
                val denominator = sinh(it)
                if (denominator == 0.0) null else 1.0 / denominator
            }
        } else {
            reduceScientificUnary(state, "csc") {
                val denominator = sin(Math.toRadians(it))
                if (denominator == 0.0) null else 1.0 / denominator
            }
        }
        CalculatorAction.Cot -> if (state.scientificTrigSecondEnabled && state.scientificHypEnabled) {
            reduceScientificUnary(state, "coth^-1") { acoth(it) }
        } else if (state.scientificTrigSecondEnabled) {
            reduceScientificUnary(state, "cot^-1") {
                if (it == 0.0) 90.0 else Math.toDegrees(atan(1.0 / it))
            }
        } else if (state.scientificHypEnabled) {
            reduceScientificUnary(state, "coth") {
                val denominator = tanh(it)
                if (denominator == 0.0) null else 1.0 / denominator
            }
        } else {
            reduceScientificUnary(state, "cot") {
                val denominator = tan(Math.toRadians(it))
                if (denominator == 0.0) null else 1.0 / denominator
            }
        }
        CalculatorAction.Floor -> reduceScientificUnary(state, "floor") { floor(it) }
        CalculatorAction.Ceil -> reduceScientificUnary(state, "ceil") { ceil(it) }
        CalculatorAction.Rand -> reduceScientificRand(state)
        CalculatorAction.ToDms -> reduceScientificToDms(state)
        CalculatorAction.ToDeg -> reduceScientificToDeg(state)
        CalculatorAction.Ln -> reduceScientificUnary(state, "ln") { if (it <= 0.0) null else ln(it) }
        CalculatorAction.Log10 -> reduceScientificUnary(state, "log") { if (it <= 0.0) null else log10(it) }
        CalculatorAction.Square -> reduceScientificUnary(state, "sqr") { it * it }
        CalculatorAction.Reciprocal -> reduceScientificUnary(state, "1/x") { if (it == 0.0) null else 1.0 / it }
        CalculatorAction.SquareRoot -> reduceScientificUnary(state, "sqrt") { if (it < 0.0) null else sqrt(it) }
        CalculatorAction.Abs -> reduceScientificUnary(state, "abs") { abs(it) }
        CalculatorAction.Exp -> reduceScientificUnary(state, "exp") { exp(it) }
        CalculatorAction.Power10 -> reduceScientificUnary(state, "10^x") { Math.pow(10.0, it) }
        CalculatorAction.Cube -> reduceScientificUnary(state, "x^3") { it * it * it }
        CalculatorAction.CubeRoot -> reduceScientificUnary(state, "3rt") { cbrt(it) }
        CalculatorAction.YRoot -> reduceScientificOperator(state, "yroot")
        CalculatorAction.Power2 -> reduceScientificUnary(state, "2^x") { Math.pow(2.0, it) }
        CalculatorAction.LogBase -> reduceScientificOperator(state, "logbase")
        CalculatorAction.Factorial -> reduceScientificFactorial(state)
        CalculatorAction.Pi -> insertScientificConstant(state, Math.PI)
        CalculatorAction.E -> insertScientificConstant(state, Math.E)
        CalculatorAction.ToggleSecond -> state.copy(
            scientificSecondEnabled = !state.scientificSecondEnabled
        )
        CalculatorAction.ToggleHyp -> state.copy(
            scientificHypEnabled = !state.scientificHypEnabled
        )
        CalculatorAction.ToggleTrigSecond -> state.copy(
            scientificTrigSecondEnabled = !state.scientificTrigSecondEnabled
        )
        CalculatorAction.Percent -> reduceScientificUnary(state, "%") { it / 100.0 }
        else -> state
    }
}

private fun asinh(x: Double): Double {
    return ln(x + kotlin.math.sqrt(x * x + 1.0))
}

private fun acosh(x: Double): Double? {
    if (x < 1.0) return null
    return ln(x + kotlin.math.sqrt((x - 1.0) * (x + 1.0)))
}

private fun atanh(x: Double): Double? {
    if (abs(x) >= 1.0) return null
    return 0.5 * ln((1.0 + x) / (1.0 - x))
}

private fun asech(x: Double): Double? {
    if (x <= 0.0 || x > 1.0) return null
    return acosh(1.0 / x)
}

private fun acsch(x: Double): Double? {
    if (x == 0.0) return null
    return asinh(1.0 / x)
}

private fun acoth(x: Double): Double? {
    if (abs(x) <= 1.0) return null
    return 0.5 * ln((x + 1.0) / (x - 1.0))
}

private fun reduceScientificRand(state: CalculatorState): CalculatorState {
    val value = Random.nextDouble()
    return state.copy(
        expressionText = "rand()",
        displayText = formatScientificResult(value),
        isNewInput = false
    )
}

private fun reduceScientificToDms(state: CalculatorState): CalculatorState {
    val input = state.displayText.toDoubleOrNull() ?: return state
    val result = decimalDegreesToDms(input)
    return state.copy(
        expressionText = "\u2192dms(${state.displayText})",
        displayText = formatScientificResult(result),
        isNewInput = false
    )
}

private fun reduceScientificToDeg(state: CalculatorState): CalculatorState {
    val input = state.displayText.toDoubleOrNull() ?: return state
    val result = dmsToDecimalDegrees(input) ?: return state.copy(
        expressionText = "\u2192deg(${state.displayText})",
        displayText = "Error",
        scientificExpression = "",
        isNewInput = true,
        firstNumber = null,
        operator = null
    )
    return state.copy(
        expressionText = "\u2192deg(${state.displayText})",
        displayText = formatScientificResult(result),
        isNewInput = false
    )
}

private fun decimalDegreesToDms(value: Double): Double {
    val sign = if (value < 0) -1.0 else 1.0
    val absValue = abs(value)
    val degrees = floor(absValue)
    val totalMinutes = (absValue - degrees) * 60.0
    val minutes = floor(totalMinutes)
    val seconds = (totalMinutes - minutes) * 60.0
    return sign * (degrees + (minutes / 100.0) + (seconds / 10000.0))
}

private fun dmsToDecimalDegrees(value: Double): Double? {
    val sign = if (value < 0) -1.0 else 1.0
    val absValue = abs(value)
    val degrees = floor(absValue)
    val minuteSecond = (absValue - degrees) * 100.0
    val minutes = floor(minuteSecond)
    val seconds = (minuteSecond - minutes) * 100.0

    if (minutes >= 60.0 || seconds >= 60.0) return null

    return sign * (degrees + (minutes / 60.0) + (seconds / 3600.0))
}

private fun reduceScientificNumber(state: CalculatorState, value: String): CalculatorState {
    val newDisplay = if (
        state.isNewInput ||
        state.displayText == "0" ||
        state.displayText == "Error" ||
        state.displayText == "-0"
    ) {
        if (state.displayText == "-0") "-$value" else value
    } else {
        state.displayText + value
    }

    if (newDisplay.count { it.isDigit() } > SCIENTIFIC_MAX_INPUT_DIGITS) {
        return state
    }

    return state.copy(
        displayText = newDisplay,
        isNewInput = false
    )
}

private fun reduceScientificDecimal(state: CalculatorState): CalculatorState {
    val text = state.displayText
    val newText = when {
        text == "Error" -> "0."
        state.isNewInput -> "0."
        text.contains(".") -> text
        else -> "$text."
    }
    return state.copy(
        displayText = newText,
        isNewInput = false
    )
}

private fun reduceScientificBackspace(state: CalculatorState): CalculatorState {
    if (state.isNewInput) return state

    val text = state.displayText
    val newText = when {
        text == "Error" -> "0"
        text.length <= 1 -> "0"
        text.startsWith("-") && text.length == 2 -> "0"
        else -> text.dropLast(1)
    }
    return state.copy(displayText = newText)
}

private fun reduceScientificToggleSign(state: CalculatorState): CalculatorState {
    val text = state.displayText
    val newText = when {
        text == "Error" -> "0"
        text.startsWith("-") -> text.removePrefix("-")
        text == "0" && state.isNewInput -> "-0"
        else -> "-$text"
    }
    return state.copy(
        displayText = newText,
        isNewInput = false
    )
}

private fun reduceScientificOperator(state: CalculatorState, rawOperator: String): CalculatorState {
    if (state.displayText == "Error") return state

    val operator = normalizeScientificOperator(rawOperator) ?: return state
    var expression = state.scientificExpression

    if (!state.isNewInput) {
        expression = appendScientificToken(expression, state.displayText)
    }

    if (expression.isBlank()) return state

    expression = when {
        scientificEndsWithOperator(expression) -> replaceScientificLastToken(expression, operator)
        scientificEndsWithOpenParen(expression) -> return state
        else -> appendScientificToken(expression, operator)
    }

    return state.copy(
        scientificExpression = expression,
        expressionText = scientificExpressionToDisplay(expression),
        isNewInput = true,
        firstNumber = null,
        operator = null
    )
}

private fun reduceScientificOpenParenthesis(state: CalculatorState): CalculatorState {
    if (state.displayText == "Error") return state

    var expression = state.scientificExpression

    if (!state.isNewInput) {
        expression = appendScientificToken(expression, state.displayText)
        expression = appendScientificToken(expression, "*")
    } else if (scientificEndsWithValue(expression)) {
        expression = appendScientificToken(expression, "*")
    }

    expression = appendScientificToken(expression, "(")

    return state.copy(
        scientificExpression = expression,
        expressionText = scientificExpressionToDisplay(expression),
        displayText = "0",
        isNewInput = true,
        firstNumber = null,
        operator = null
    )
}

private fun reduceScientificCloseParenthesis(state: CalculatorState): CalculatorState {
    if (state.displayText == "Error") return state

    var expression = state.scientificExpression
    if (!state.isNewInput) {
        expression = appendScientificToken(expression, state.displayText)
    }

    if (!canAppendScientificCloseParenthesis(expression)) return state

    expression = appendScientificToken(expression, ")")

    return state.copy(
        scientificExpression = expression,
        expressionText = scientificExpressionToDisplay(expression),
        isNewInput = true,
        firstNumber = null,
        operator = null
    )
}

private fun reduceScientificEquals(state: CalculatorState): CalculatorState {
    if (state.displayText == "Error") return state

    var expression = state.scientificExpression
    if (!state.isNewInput) {
        expression = appendScientificToken(expression, state.displayText)
    }

    if (expression.isBlank()) return state
    if (scientificEndsWithOperator(expression) || scientificEndsWithOpenParen(expression)) return state

    val result = evaluateScientificExpression(expression) ?: return state.copy(
        expressionText = "${scientificExpressionToDisplay(expression)} =",
        displayText = "Error",
        scientificExpression = "",
        isNewInput = true,
        firstNumber = null,
        operator = null
    )

    val formatted = formatScientificResult(result)
    return state.copy(
        expressionText = "${scientificExpressionToDisplay(expression)} =",
        displayText = formatted,
        scientificExpression = "",
        isNewInput = true,
        firstNumber = null,
        operator = null
    )
}

private fun reduceScientificUnary(
    state: CalculatorState,
    label: String,
    transform: (Double) -> Double?
): CalculatorState {
    val current = state.displayText.toDoubleOrNull() ?: return state
    val result = transform(current) ?: return state.copy(
        expressionText = "$label(${state.displayText})",
        displayText = "Error",
        scientificExpression = "",
        isNewInput = true,
        firstNumber = null,
        operator = null
    )

    val expressionText = if (state.scientificExpression.isBlank()) {
        "$label(${state.displayText})"
    } else {
        scientificExpressionToDisplay(state.scientificExpression)
    }

    return state.copy(
        expressionText = expressionText,
        displayText = formatScientificResult(result),
        isNewInput = false
    )
}

private fun reduceScientificFactorial(state: CalculatorState): CalculatorState {
    val input = state.displayText.toDoubleOrNull() ?: return state
    if (input < 0.0) return state.copy(displayText = "Error", scientificExpression = "", isNewInput = true)

    val asLong = input.toLong()
    if (input != asLong.toDouble()) return state.copy(displayText = "Error", scientificExpression = "", isNewInput = true)
    if (asLong > 170) return state.copy(displayText = "Error", scientificExpression = "", isNewInput = true)

    var result = 1.0
    for (i in 2..asLong) {
        result *= i.toDouble()
    }

    val expressionText = if (state.scientificExpression.isBlank()) {
        "${state.displayText}!"
    } else {
        scientificExpressionToDisplay(state.scientificExpression)
    }

    return state.copy(
        expressionText = expressionText,
        displayText = formatScientificResult(result),
        isNewInput = false
    )
}

private fun insertScientificConstant(state: CalculatorState, value: Double): CalculatorState {
    val formatted = formatScientificResult(value)
    var expression = state.scientificExpression

    if (!state.isNewInput && state.displayText != "Error") {
        expression = appendScientificToken(expression, state.displayText)
        expression = appendScientificToken(expression, "*")
    } else if (scientificEndsWithValue(expression)) {
        expression = appendScientificToken(expression, "*")
    }

    return state.copy(
        scientificExpression = expression,
        expressionText = scientificExpressionToDisplay(expression),
        displayText = formatted,
        isNewInput = false
    )
}

private fun appendScientificToken(expression: String, token: String): String {
    return if (expression.isBlank()) token else "$expression $token"
}

private fun replaceScientificLastToken(expression: String, token: String): String {
    val tokens = scientificTokens(expression).toMutableList()
    if (tokens.isEmpty()) return token
    tokens[tokens.lastIndex] = token
    return tokens.joinToString(" ")
}

private fun normalizeScientificOperator(raw: String): String? {
    return when (raw.trim()) {
        "+" -> "+"
        "-" -> "-"
        "*", "\u00d7" -> "*"
        "/", "\u00f7" -> "/"
        "^" -> "^"
        "yroot" -> "yroot"
        "logbase" -> "logbase"
        "mod", "MOD", "%" -> "mod"
        else -> null
    }
}

private fun scientificTokens(expression: String): List<String> {
    if (expression.isBlank()) return emptyList()
    return expression.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
}

private fun scientificEndsWithOperator(expression: String): Boolean {
    val last = scientificTokens(expression).lastOrNull() ?: return false
    return isScientificOperator(last)
}

private fun scientificEndsWithOpenParen(expression: String): Boolean {
    return scientificTokens(expression).lastOrNull() == "("
}

private fun scientificEndsWithValue(expression: String): Boolean {
    val last = scientificTokens(expression).lastOrNull() ?: return false
    return last == ")" || last.toDoubleOrNull() != null
}

private fun canAppendScientificCloseParenthesis(expression: String): Boolean {
    val tokens = scientificTokens(expression)
    if (tokens.isEmpty()) return false

    val openCount = tokens.count { it == "(" }
    val closeCount = tokens.count { it == ")" }
    if (closeCount >= openCount) return false

    val last = tokens.last()
    return last == ")" || last.toDoubleOrNull() != null
}

private fun scientificExpressionToDisplay(expression: String): String {
    return scientificTokens(expression).joinToString(" ") {
        when (it) {
            "*" -> "\u00d7"
            "/" -> "\u00f7"
            "yroot" -> "\u02b8\u221ax"
            "logbase" -> "log\u1d67x"
            else -> it
        }
    }
}

private fun evaluateScientificExpression(expression: String): Double? {
    val tokens = scientificTokens(expression)
    if (tokens.isEmpty()) return null

    val output = mutableListOf<String>()
    val operators = ArrayDeque<String>()

    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> output += token
            token == "(" -> operators.addLast(token)
            token == ")" -> {
                while (operators.isNotEmpty() && operators.last() != "(") {
                    output += operators.removeLast()
                }
                if (operators.isEmpty() || operators.last() != "(") return null
                operators.removeLast()
            }
            isScientificOperator(token) -> {
                while (
                    operators.isNotEmpty() &&
                    isScientificOperator(operators.last()) &&
                    shouldPopScientificOperator(token, operators.last())
                ) {
                    output += operators.removeLast()
                }
                operators.addLast(token)
            }
            else -> return null
        }
    }

    while (operators.isNotEmpty()) {
        val operator = operators.removeLast()
        if (operator == "(" || operator == ")") return null
        output += operator
    }

    val stack = ArrayDeque<Double>()
    for (token in output) {
        val number = token.toDoubleOrNull()
        if (number != null) {
            stack.addLast(number)
            continue
        }

        val right = stack.removeLastOrNull() ?: return null
        val left = stack.removeLastOrNull() ?: return null

        val result = when (token) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> if (right == 0.0) return null else left / right
            "mod" -> if (right == 0.0) return null else left % right
            "^" -> Math.pow(left, right)
            "yroot" -> {
                if (right == 0.0) return null
                Math.pow(left, 1.0 / right)
            }
            "logbase" -> {
                if (left <= 0.0 || right <= 0.0 || right == 1.0) return null
                ln(left) / ln(right)
            }
            else -> return null
        }

        if (result.isNaN() || result.isInfinite()) return null
        stack.addLast(result)
    }

    if (stack.size != 1) return null
    return stack.last()
}

private fun isScientificOperator(token: String): Boolean {
    return token in setOf("+", "-", "*", "/", "mod", "^", "yroot", "logbase")
}

private fun scientificPrecedence(token: String): Int {
    return when (token) {
        "^", "yroot", "logbase" -> 4
        "*", "/", "mod" -> 3
        "+", "-" -> 2
        else -> 0
    }
}

private fun shouldPopScientificOperator(incoming: String, stackTop: String): Boolean {
    val incomingPrecedence = scientificPrecedence(incoming)
    val stackPrecedence = scientificPrecedence(stackTop)
    if (incoming == "^" || incoming == "yroot" || incoming == "logbase") {
        return incomingPrecedence < stackPrecedence
    }
    return incomingPrecedence <= stackPrecedence
}

private fun formatScientificResult(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "Error"
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}

