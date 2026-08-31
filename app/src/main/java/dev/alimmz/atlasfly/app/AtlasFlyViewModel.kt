package dev.alimmz.atlasfly.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.model.AuthError
import auth.model.AuthResult
import auth.usecase.IsAuthorizedUseCase
import auth.usecase.IsEmailVerifiedUseCase
import auth.usecase.VerifyEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dev.alimmz.atlasfly.app.deeplink.AuthDeepLink
import dev.alimmz.atlasfly.app.deeplink.AuthDeepLinkParser
import dev.alimmz.atlasfly.R
import dev.alimmz.atlasfly.core.navigation.Routes
import javax.inject.Inject

@HiltViewModel
class AtlasFlyViewModel @Inject constructor(
    private val isAuthorizedUseCase: IsAuthorizedUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val isEmailVerifiedUseCase: IsEmailVerifiedUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AtlasFlyUiState())
    val uiState: StateFlow<AtlasFlyUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: AtlasFlyEvent) {
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
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthorized = isAuthorized,
                )
            }
        }
    }

    private fun handleDeepLink(uri: android.net.Uri) {
        viewModelScope.launch {
            when (val deepLink: AuthDeepLink? = AuthDeepLinkParser.parse(uri)) {
                is AuthDeepLink.VerifyEmail -> applyEmailVerification(deepLink.oobCode)
                AuthDeepLink.EmailVerifiedLanding -> refreshEmailVerificationStatus()
                is AuthDeepLink.ResetPassword -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingNavigation = Routes.Auth.ResetPassword(deepLink.oobCode),
                    )
                }
                AuthDeepLink.PasswordResetLanding -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        pendingNavigation = Routes.Auth.ForgotPassword(),
                    )
                }
                null -> Unit
            }
        }
    }

    private suspend fun applyEmailVerification(oobCode: String) {
        verifyEmailUseCase(oobCode).collect { result ->
            when (result) {
                is AuthResult.Loading -> _uiState.update {
                    it.copy(isLoading = true, errorMessage = null)
                }
                is AuthResult.Success -> onEmailVerified()
                is AuthResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.error.toMessageRes(),
                    )
                }
            }
        }
    }

    private suspend fun refreshEmailVerificationStatus() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val isVerified: Boolean = isEmailVerifiedUseCase.invoke()
        if (isVerified) {
            onEmailVerified()
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = AuthError.EmailNotVerified.toMessageRes(),
                )
            }
        }
    }

    private fun onEmailVerified() {
        _uiState.update {
            it.copy(
                isLoading = false,
                isAuthorized = true,
                emailVerificationMessage = R.string.email_verified_success,
                pendingNavigation = Routes.Home,
                errorMessage = null,
            )
        }
    }

    private fun logout() {
        viewModelScope.launch {
        }
    }

    private fun AuthError.toMessageRes(): Int {
        return when (this) {
            AuthError.InvalidActionCode -> R.string.error_invalid_action_code
            AuthError.EmailNotVerified -> R.string.error_email_not_verified
            AuthError.NetworkError -> R.string.error_network
            AuthError.TooManyAttempts -> R.string.error_too_many
            else -> R.string.error_unknown
        }
    }
}
