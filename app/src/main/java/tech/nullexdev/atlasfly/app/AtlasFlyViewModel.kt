package tech.nullexdev.atlasfly.app

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
import tech.nullexdev.atlasfly.app.deeplink.EmailVerificationDeepLink
import tech.nullexdev.atlasfly.app.deeplink.EmailVerificationDeepLinkParser
import tech.nullexdev.atlasfly.core.navigation.Routes
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
            when (val deepLink: EmailVerificationDeepLink? = EmailVerificationDeepLinkParser.parse(uri)) {
                is EmailVerificationDeepLink.ActionCode -> applyEmailVerification(deepLink.oobCode)
                EmailVerificationDeepLink.VerifiedLanding -> refreshEmailVerificationStatus()
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
                        errorMessage = result.error.toMessage(),
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
                    errorMessage = AuthError.EmailNotVerified.toMessage(),
                )
            }
        }
    }

    private fun onEmailVerified() {
        _uiState.update {
            it.copy(
                isLoading = false,
                isAuthorized = true,
                emailVerificationMessage = "Email verified successfully",
                pendingNavigation = Routes.Home,
                errorMessage = null,
            )
        }
    }

    private fun logout() {
        viewModelScope.launch {
        }
    }

    private fun AuthError.toMessage(): String {
        return when (this) {
            AuthError.InvalidActionCode -> "This verification link is invalid or has expired"
            AuthError.EmailNotVerified -> "Email is not verified yet. Check your inbox and try again"
            AuthError.NetworkError -> "Network error. Please try again"
            AuthError.TooManyAttempts -> "Too many attempts. Please try again later"
            else -> "Something went wrong. Please try again"
        }
    }
}
