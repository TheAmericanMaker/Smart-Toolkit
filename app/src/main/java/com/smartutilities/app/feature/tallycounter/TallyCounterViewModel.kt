package com.smartutilities.app.feature.tallycounter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

data class TallyCounterUiState(
    val count: Int = 0
)

@HiltViewModel
class TallyCounterViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _count = savedStateHandle.getStateFlow("count", 0)
    val uiState: StateFlow<TallyCounterUiState> = _count.map { TallyCounterUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TallyCounterUiState())

    fun increment() {
        savedStateHandle["count"] = _count.value + 1
    }

    fun decrement() {
        savedStateHandle["count"] = maxOf(0, _count.value - 1)
    }

    fun reset() {
        savedStateHandle["count"] = 0
    }
}
