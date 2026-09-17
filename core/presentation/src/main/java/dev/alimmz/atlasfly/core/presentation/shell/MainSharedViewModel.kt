package dev.alimmz.atlasfly.core.presentation.shell

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Shared scope for the top-level destinations.
 *
 * The app shell resolves a single instance and hands it to every tab, so state
 * published by one tab is visible to its siblings.
 */
@HiltViewModel
class MainSharedViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MainSharedUiState())
    val uiState: StateFlow<MainSharedUiState> = _uiState.asStateFlow()

    fun showMessage(@StringRes message: Int) {
        _uiState.update { it.copy(message = message) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }
}
