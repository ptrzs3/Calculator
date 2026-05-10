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
    val scientificTrigSecondEnabled: Boolean = false,
    // ADC mode
    val adcDirection: AdcDirection = AdcDirection.DIGITAL_TO_ANALOG,
    val adcResolution: Int = 12,
    val adcVrefPlus: Double = 3.3,
    val adcVrefMinus: Double = 0.0,
    val adcEncoding: AdcEncoding = AdcEncoding.STRAIGHT_BINARY,
    val adcDigitalValue: Long = 0L,
    val adcAnalogValue: Double = 0.0,
    val adcPresetIndex: Int = 0
)
