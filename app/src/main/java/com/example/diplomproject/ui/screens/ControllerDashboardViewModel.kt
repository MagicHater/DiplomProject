package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.data.remote.ControllerDashboardResponseDto
import com.example.diplomproject.data.repository.ControllerDashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControllerDashboardViewModel @Inject constructor(
    private val repo: ControllerDashboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ControllerDashboardResponseDto?>(null)
    val state: StateFlow<ControllerDashboardResponseDto?> = _state.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            runCatching { repo.getDashboard() }
                .onSuccess { _state.value = it }
            _loading.value = false
        }
    }
}
