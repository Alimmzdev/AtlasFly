package tech.nullexdev.atlasfly.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AtlasFlyViewModel @Inject constructor(
    // private val authRepository: AuthRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(AtlasFlyUiState())
    val uiState: StateFlow<AtlasFlyUiState> = _uiState.asStateFlow()


    init {
        loadData()
    }


    private fun loadData() {
        viewModelScope.launch {
            // Load data from repository
            delay(2000.milliseconds)
            _uiState.value = _uiState.value.copy(
                isLoading = false
            )
        }
    }


    fun onEvent(event: AtlasFlyEvent) {
        when (event) {

            AtlasFlyEvent.Refresh -> {
                loadData()
            }

            AtlasFlyEvent.Logout -> {
                logout()
            }
        }
    }


    private fun logout() {
        viewModelScope.launch {

            // authRepository.logout()

        }
    }
}