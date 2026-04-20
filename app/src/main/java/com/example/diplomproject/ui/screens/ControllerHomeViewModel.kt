package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomproject.domain.model.TestCategory
import com.example.diplomproject.domain.repository.TestSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ControllerHomeViewModel @Inject constructor(
    private val testSessionRepository: TestSessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ControllerHomeUiState())
    val uiState: StateFlow<ControllerHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            runCatching { testSessionRepository.getCategories() }
                .onSuccess { categories ->
                    _uiState.update { it.copy(categories = categories, selectedCategoryId = categories.firstOrNull()?.id) }
                }
        }
    }

    fun onCategorySelected(id: String) {
        _uiState.update { it.copy(selectedCategoryId = id) }
    }

    fun generateToken() {
        val categoryId = _uiState.value.selectedCategoryId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { testSessionRepository.createControllerToken(categoryId) }
                .onSuccess { token -> _uiState.update { it.copy(isLoading = false, generatedToken = token.token) } }
                .onFailure { _uiState.update { it.copy(isLoading = false) } }
        }
    }
}

data class ControllerHomeUiState(
    val isLoading: Boolean = false,
    val categories: List<TestCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val generatedToken: String = "",
)
