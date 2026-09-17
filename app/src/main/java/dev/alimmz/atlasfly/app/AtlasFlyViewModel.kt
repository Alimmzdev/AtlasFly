package dev.alimmz.atlasfly.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.usecase.GetUnverifiedUserEmailUseCase
import auth.usecase.IsAuthorizedUseCase
import auth.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dev.alimmz.atlasfly.app.logging.UiLogger
import dev.alimmz.atlasfly.app.deeplink.AuthDeepLink
import dev.alimmz.atlasfly.app.deeplink.AuthDeepLinkParser
import dev.alimmz.atlasfly.core.navigation.Routes
import javax.inject.Inject

@HiltViewModel
class AtlasFlyViewModel @Inject constructor(
    private val isAuthorizedUseCase: IsAuthorizedUseCase,
    private val getUnverifiedUserEmailUseCase: GetUnverifiedUserEmailUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AtlasFlyUiState())
    val uiState: StateFlow<AtlasFlyUiState> = _uiState.asStateFlow()

    init {
        UiLogger.observeState(viewModelScope, "AtlasFlyViewModel", uiState)
        loadData()
    }

    fun onEvent(event: AtlasFlyEvent) {
        UiLogger.logEvent("AtlasFlyViewModel.Event", event)
        when (event) {
            AtlasFlyEvent.Refresh -> loadData()
            AtlasFlyEvent.Logout -> logout()
            is AtlasFlyEvent.HandleDeepLink -> handleDeepLink(event.uri)
            AtlasFlyEvent.DeepLinkHandled -> {
                _uiState.update { it.copy(pendingNavigation = null) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val isAuthorized: Boolean = isAuthorizedUseCase.invoke()
            if (isAuthorized) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthorized = true,
                    )
                }
                return@launch
            }

            val unverifiedEmail: String? = getUnverifiedUserEmailUseCase.invoke()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthorized = false,
                    pendingNavigation = unverifiedEmail?.let { email ->
                        Routes.Auth.SignUpEmailVerification(email)
                    } ?: it.pendingNavigation,
                )
            }
        }
    }

    private fun handleDeepLink(uri: android.net.Uri) {
        viewModelScope.launch {
            when (val deepLink: AuthDeepLink? = AuthDeepLinkParser.parse(uri)) {
                AuthDeepLink.PasswordRecovery -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingNavigation = Routes.Auth.ResetPassword,
                    )
                }
                AuthDeepLink.SessionCallback -> loadData()
                null -> Unit
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update {
                it.copy(
                    isAuthorized = false,
                    isLoading = false,
                    pendingNavigation = Routes.Auth.Login,
                )
            }
        }
    }

}
