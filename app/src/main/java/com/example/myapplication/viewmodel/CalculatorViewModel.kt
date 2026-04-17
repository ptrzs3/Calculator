package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.model.CalculatorAction
import com.example.myapplication.model.CalculatorState
import com.example.myapplication.reducer.reduceCalculatorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onAction(action: CalculatorAction) {
        _state.value = reduceCalculatorState(_state.value, action)
    }
}