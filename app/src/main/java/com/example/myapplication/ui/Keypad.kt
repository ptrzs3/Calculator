package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.CalculatorAction
import com.example.myapplication.model.CalculatorMode

data class CalculatorKey(
    val label: String,
    val action: CalculatorAction,
    val enabled: Boolean = true
)

@Composable
fun Keypad(
    mode: CalculatorMode,
    scientificSecondEnabled: Boolean,
    programmerBase: Int,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val keypadRows = when (mode) {
        CalculatorMode.STANDARD -> standardRows()
        CalculatorMode.SCIENTIFIC -> scientificRows(scientificSecondEnabled)
        CalculatorMode.PROGRAMMER -> programmerRows(programmerBase)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                row.forEach { key ->
                    val style = when {
                        mode == CalculatorMode.SCIENTIFIC &&
                            key.action == CalculatorAction.ToggleSecond &&
                            scientificSecondEnabled -> CalculatorButtonStyle.Equals
                        key.action is CalculatorAction.Equals -> CalculatorButtonStyle.Equals
                        key.action is CalculatorAction.Number ||
                            key.action == CalculatorAction.Decimal ||
                            key.action == CalculatorAction.ToggleSign -> CalculatorButtonStyle.Number
                        key.action is CalculatorAction.Operator -> CalculatorButtonStyle.Operator
                        isFunctionAction(key.action) -> CalculatorButtonStyle.Function
                        else -> CalculatorButtonStyle.Number
                    }

                    CalculatorButton(
                        label = key.label,
                        modifier = Modifier.weight(1f),
                        style = style,
                        enabled = key.enabled
                    ) {
                        onAction(key.action)
                    }
                }
            }
        }
    }
}

private fun isFunctionAction(action: CalculatorAction): Boolean {
    return action in setOf(
        CalculatorAction.Clear,
        CalculatorAction.ClearEntry,
        CalculatorAction.Backspace,
        CalculatorAction.Percent,
        CalculatorAction.Reciprocal,
        CalculatorAction.Square,
        CalculatorAction.SquareRoot,
        CalculatorAction.Cube,
        CalculatorAction.CubeRoot,
        CalculatorAction.YRoot,
        CalculatorAction.Power2,
        CalculatorAction.LogBase,
        CalculatorAction.Sin,
        CalculatorAction.Cos,
        CalculatorAction.Tan,
        CalculatorAction.Ln,
        CalculatorAction.Log10,
        CalculatorAction.Pi,
        CalculatorAction.E,
        CalculatorAction.Power,
        CalculatorAction.Not,
        CalculatorAction.And,
        CalculatorAction.Or,
        CalculatorAction.Xor,
        CalculatorAction.ShiftLeft,
        CalculatorAction.ShiftRight,
        CalculatorAction.ToggleSecond,
        CalculatorAction.Abs,
        CalculatorAction.Exp,
        CalculatorAction.Factorial,
        CalculatorAction.OpenParenthesis,
        CalculatorAction.CloseParenthesis,
        CalculatorAction.Power10
    ) || action is CalculatorAction.ChangeBase || action is CalculatorAction.ChangeWordSize
}

private fun standardRows() = listOf(
    listOf(
        CalculatorKey("%", CalculatorAction.Percent),
        CalculatorKey("CE", CalculatorAction.ClearEntry),
        CalculatorKey("C", CalculatorAction.Clear),
        CalculatorKey("\u232b", CalculatorAction.Backspace)
    ),
    listOf(
        CalculatorKey("1/x", CalculatorAction.Reciprocal),
        CalculatorKey("x\u00b2", CalculatorAction.Square),
        CalculatorKey("\u221ax", CalculatorAction.SquareRoot),
        CalculatorKey("\u00f7", CalculatorAction.Operator("\u00f7"))
    ),
    listOf(
        CalculatorKey("7", CalculatorAction.Number("7")),
        CalculatorKey("8", CalculatorAction.Number("8")),
        CalculatorKey("9", CalculatorAction.Number("9")),
        CalculatorKey("\u00d7", CalculatorAction.Operator("\u00d7"))
    ),
    listOf(
        CalculatorKey("4", CalculatorAction.Number("4")),
        CalculatorKey("5", CalculatorAction.Number("5")),
        CalculatorKey("6", CalculatorAction.Number("6")),
        CalculatorKey("-", CalculatorAction.Operator("-"))
    ),
    listOf(
        CalculatorKey("1", CalculatorAction.Number("1")),
        CalculatorKey("2", CalculatorAction.Number("2")),
        CalculatorKey("3", CalculatorAction.Number("3")),
        CalculatorKey("+", CalculatorAction.Operator("+"))
    ),
    listOf(
        CalculatorKey("+/-", CalculatorAction.ToggleSign),
        CalculatorKey("0", CalculatorAction.Number("0")),
        CalculatorKey(".", CalculatorAction.Decimal),
        CalculatorKey("=", CalculatorAction.Equals)
    )
)

private fun scientificRows(scientificSecondEnabled: Boolean) = listOf(
    listOf(
        CalculatorKey("2nd", CalculatorAction.ToggleSecond),
        CalculatorKey("\u03c0", CalculatorAction.Pi),
        CalculatorKey("e", CalculatorAction.E),
        CalculatorKey("C", CalculatorAction.Clear),
        CalculatorKey("\u232b", CalculatorAction.Backspace)
    ),
    listOf(
        CalculatorKey(
            if (scientificSecondEnabled) "x\u00b3" else "x\u00b2",
            if (scientificSecondEnabled) CalculatorAction.Cube else CalculatorAction.Square
        ),
        CalculatorKey("1/x", CalculatorAction.Reciprocal),
        CalculatorKey("|x|", CalculatorAction.Abs),
        CalculatorKey("exp", CalculatorAction.Exp),
        CalculatorKey("mod", CalculatorAction.Operator("mod"))
    ),
    listOf(
        CalculatorKey(
            if (scientificSecondEnabled) "\u00b3\u221ax" else "\u00b2\u221ax",
            if (scientificSecondEnabled) CalculatorAction.CubeRoot else CalculatorAction.SquareRoot
        ),
        CalculatorKey("(", CalculatorAction.OpenParenthesis),
        CalculatorKey(")", CalculatorAction.CloseParenthesis),
        CalculatorKey("n!", CalculatorAction.Factorial),
        CalculatorKey("\u00f7", CalculatorAction.Operator("\u00f7"))
    ),
    listOf(
        CalculatorKey(
            if (scientificSecondEnabled) "\u02b8\u221ax" else "x\u02b8",
            if (scientificSecondEnabled) CalculatorAction.YRoot else CalculatorAction.Power
        ),
        CalculatorKey("7", CalculatorAction.Number("7")),
        CalculatorKey("8", CalculatorAction.Number("8")),
        CalculatorKey("9", CalculatorAction.Number("9")),
        CalculatorKey("\u00d7", CalculatorAction.Operator("\u00d7"))
    ),
    listOf(
        CalculatorKey(
            if (scientificSecondEnabled) "2\u02e3" else "10\u02e3",
            if (scientificSecondEnabled) CalculatorAction.Power2 else CalculatorAction.Power10
        ),
        CalculatorKey("4", CalculatorAction.Number("4")),
        CalculatorKey("5", CalculatorAction.Number("5")),
        CalculatorKey("6", CalculatorAction.Number("6")),
        CalculatorKey("-", CalculatorAction.Operator("-"))
    ),
    listOf(
        CalculatorKey(
            if (scientificSecondEnabled) "log\u1d67x" else "log",
            if (scientificSecondEnabled) CalculatorAction.LogBase else CalculatorAction.Log10
        ),
        CalculatorKey("1", CalculatorAction.Number("1")),
        CalculatorKey("2", CalculatorAction.Number("2")),
        CalculatorKey("3", CalculatorAction.Number("3")),
        CalculatorKey("+", CalculatorAction.Operator("+"))
    ),
    listOf(
        CalculatorKey(
            if (scientificSecondEnabled) "e\u02e3" else "ln",
            if (scientificSecondEnabled) CalculatorAction.Exp else CalculatorAction.Ln
        ),
        CalculatorKey("+/-", CalculatorAction.ToggleSign),
        CalculatorKey("0", CalculatorAction.Number("0")),
        CalculatorKey(".", CalculatorAction.Decimal),
        CalculatorKey("=", CalculatorAction.Equals)
    )
)

private fun programmerRows(base: Int) = listOf(
    listOf(
        CalculatorKey("A", CalculatorAction.Number("A"), enabled = base > 10),
        CalculatorKey("<<", CalculatorAction.ShiftLeft),
        CalculatorKey(">>", CalculatorAction.ShiftRight),
        CalculatorKey("C", CalculatorAction.Clear),
        CalculatorKey("\u232b", CalculatorAction.Backspace)
    ),
    listOf(
        CalculatorKey("B", CalculatorAction.Number("B"), enabled = base > 11),
        CalculatorKey("(", CalculatorAction.OpenParenthesis),
        CalculatorKey(")", CalculatorAction.CloseParenthesis),
        CalculatorKey("%", CalculatorAction.Operator("mod")),
        CalculatorKey("\u00f7", CalculatorAction.Operator("\u00f7"))
    ),
    listOf(
        CalculatorKey("C", CalculatorAction.Number("C"), enabled = base > 12),
        CalculatorKey("7", CalculatorAction.Number("7"), enabled = base > 7),
        CalculatorKey("8", CalculatorAction.Number("8"), enabled = base > 8),
        CalculatorKey("9", CalculatorAction.Number("9"), enabled = base > 9),
        CalculatorKey("\u00d7", CalculatorAction.Operator("\u00d7"))
    ),
    listOf(
        CalculatorKey("D", CalculatorAction.Number("D"), enabled = base > 13),
        CalculatorKey("4", CalculatorAction.Number("4"), enabled = base > 4),
        CalculatorKey("5", CalculatorAction.Number("5"), enabled = base > 5),
        CalculatorKey("6", CalculatorAction.Number("6"), enabled = base > 6),
        CalculatorKey("-", CalculatorAction.Operator("-"))
    ),
    listOf(
        CalculatorKey("E", CalculatorAction.Number("E"), enabled = base > 14),
        CalculatorKey("1", CalculatorAction.Number("1"), enabled = base > 1),
        CalculatorKey("2", CalculatorAction.Number("2"), enabled = base > 2),
        CalculatorKey("3", CalculatorAction.Number("3"), enabled = base > 3),
        CalculatorKey("+", CalculatorAction.Operator("+"))
    ),
    listOf(
        CalculatorKey("F", CalculatorAction.Number("F"), enabled = base > 15),
        CalculatorKey("+/-", CalculatorAction.ToggleSign),
        CalculatorKey("0", CalculatorAction.Number("0")),
        CalculatorKey(".", CalculatorAction.Decimal, enabled = false),
        CalculatorKey("=", CalculatorAction.Equals)
    )
)
