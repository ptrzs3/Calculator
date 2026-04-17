package com.example.myapplication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.CalculatorAction
import com.example.myapplication.model.CalculatorMode
import com.example.myapplication.model.ProgrammerInputPane
import com.example.myapplication.viewmodel.CalculatorViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onShowInfo: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            if (state.mode == CalculatorMode.PROGRAMMER) {
                Display(
                    expressionText = state.expressionText,
                    displayText = state.displayText,
                    preferTrailingEdge = !state.isNewInput
                )

                Spacer(modifier = Modifier.height(8.dp))

                ProgrammerPanel(
                    value = state.programmerValue,
                    currentBase = state.programmerBase,
                    wordSize = state.wordSize,
                    inputPane = state.programmerInputPane,
                    shiftMode = state.programmerShiftMode,
                    onBaseChange = { viewModel.onAction(CalculatorAction.ChangeBase(it)) },
                    onWordSizeChange = { viewModel.onAction(CalculatorAction.ChangeWordSize(it)) },
                    onToggleInputPane = {
                        val nextPane = if (state.programmerInputPane == ProgrammerInputPane.KEYPAD) {
                            ProgrammerInputPane.BITS
                        } else {
                            ProgrammerInputPane.KEYPAD
                        }
                        viewModel.onAction(CalculatorAction.ChangeProgrammerInputPane(nextPane))
                    },
                    onBitwiseAction = { viewModel.onAction(it) },
                    onShiftModeChange = { viewModel.onAction(CalculatorAction.ChangeProgrammerShiftMode(it)) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Display(
                    expressionText = state.expressionText,
                    displayText = state.displayText,
                    preferTrailingEdge = !state.isNewInput
                )

                if (state.mode == CalculatorMode.SCIENTIFIC) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ScientificFunctionMenus(
                        scientificTrigSecondEnabled = state.scientificTrigSecondEnabled,
                        scientificHypEnabled = state.scientificHypEnabled,
                        onAction = { action -> viewModel.onAction(action) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (state.mode == CalculatorMode.PROGRAMMER) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val keySpacing = 2.dp
                    val columns = 5
                    val rows = 6
                    val buttonAspect = 1.67f
                    val buttonWidth = (maxWidth - keySpacing * (columns - 1)) / columns.toFloat()
                    val keyboardHeight = (buttonWidth / buttonAspect) * rows + keySpacing * (rows - 1)

                    if (state.programmerInputPane == ProgrammerInputPane.BITS) {
                        ProgrammerBitKeyboard(
                            value = state.programmerValue,
                            wordSize = state.wordSize,
                            onToggleBit = { viewModel.onAction(CalculatorAction.ToggleProgrammerBit(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(keyboardHeight)
                        )
                    } else {
                        Keypad(
                            mode = state.mode,
                            scientificSecondEnabled = state.scientificSecondEnabled,
                            programmerBase = state.programmerBase,
                            onAction = { action -> viewModel.onAction(action) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(keyboardHeight)
                        )
                    }
                }
            } else {
                Keypad(
                    mode = state.mode,
                    scientificSecondEnabled = state.scientificSecondEnabled,
                    programmerBase = state.programmerBase,
                    onAction = { action -> viewModel.onAction(action) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 14.dp)
        ) {
            ModeSelector(
                currentMode = state.mode,
                onModeChange = { viewModel.onAction(CalculatorAction.ChangeMode(it)) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            ThemeToggleButton(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        InfoButton(
            onClick = onShowInfo,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 14.dp)
        )
    }
}
