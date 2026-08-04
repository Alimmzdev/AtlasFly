package tech.nullexdev.atlasfly.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.usecase.IsAuthorizedUseCase
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
    private val isAuthorizedUseCase: IsAuthorizedUseCase,
) : ViewModel() {


    private val _uiState = MutableStateFlow(AtlasFlyUiState())
    val uiState: StateFlow<AtlasFlyUiState> = _uiState.asStateFlow()


    init {
        loadData()
    }


    private fun loadData() {
        viewModelScope.launch {
            val isAuthorized = isAuthorizedUseCase.invoke()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isAuthorized = isAuthorized,
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