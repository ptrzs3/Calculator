package com.example.myapplication.model

data class CalculatorState(
    val mode: CalculatorMode = CalculatorMode.STANDARD,
    val expressionText: String = "",
    val displayText: String = "0",
    val firstNumber: Double? = null,
    val operator: String? = null,
    val isNewInput: Boolean = true,
    val programmerValue: Long = 0L,
    val firstProgrammerValue: Long? = null,
    val programmerOperator: String? = null,
    val programmerBase: Int = 10,
    val wordSize: Int = 64,
    val programmerInputPane: ProgrammerInputPane = ProgrammerInputPane.KEYPAD,
    val programmerShiftMode: ProgrammerShiftMode = ProgrammerShiftMode.ARITHMETIC,
    val scientificExpression: String = "",
    val scientificSecondEnabled: Boolean = false,
    val scientificHypEnabled: Boolean = false,
    val scientificTrigSecondEnabled: Boolean = false
)
