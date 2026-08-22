package com.alimmzdev.atlasfly.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.model.AuthError
import auth.model.AuthResult
import auth.usecase.IsEmailVerifiedUseCase
import auth.usecase.ResendEmailVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpEmailVerificationViewModel @Inject constructor(
    private val resendEmailVerificationUseCase: ResendEmailVerificationUseCase,
    private val isEmailVerifiedUseCase: IsEmailVerifiedUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<SignUpEmailVerificationUiState> =
        MutableStateFlow(SignUpEmailVerificationUiState())
    val uiState: StateFlow<SignUpEmailVerificationUiState> = _uiState.asStateFlow()

    private val _events = Channel<SignUpEmailVerificationEvent>()
    val events = _events.receiveAsFlow()

    fun onIntent(intent: SignUpEmailVerificationUiIntent) {
        when (intent) {
            SignUpEmailVerificationUiIntent.ResendEmailClicked -> resendVerificationEmail()
            SignUpEmailVerificationUiIntent.CheckVerificationClicked -> checkVerificationStatus()
            SignUpEmailVerificationUiIntent.DismissMessage -> {
                _uiState.update { it.copy(message = null, error = null) }
            }
        }
    }

    private fun resendVerificationEmail() {
        viewModelScope.launch {
            resendEmailVerificationUseCase.invoke().collect { result ->
                when (result) {
                    is AuthResult.Loading -> _uiState.update {
                        it.copy(isLoading = true, error = null)
                    }
                    is AuthResult.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Verification email sent",
                        )
                    }
                    is AuthResult.Failure -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error.toMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun checkVerificationStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val isVerified: Boolean = isEmailVerifiedUseCase.invoke()
            if (isVerified) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(SignUpEmailVerificationEvent.NavigateHome)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AuthError.EmailNotVerified.toMessage(),
                    )
                }
            }
        }
    }

    private fun AuthError.toMessage(): String {
        return when (this) {
            AuthError.EmailNotVerified -> "Email is not verified yet. Check your inbox and try again"
            AuthError.TooManyAttempts -> "Too many attempts. Please try again later"
            AuthError.NetworkError -> "Network error. Please try again"
            else -> "Something went wrong. Please try again"
        }
    }
}

data class SignUpEmailVerificationUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

sealed interface SignUpEmailVerificationUiIntent {
    data object ResendEmailClicked : SignUpEmailVerificationUiIntent
    data object CheckVerificationClicked : SignUpEmailVerificationUiIntent
    data object DismissMessage : SignUpEmailVerificationUiIntent
}

sealed interface SignUpEmailVerificationEvent {
    data object NavigateHome : SignUpEmailVerificationEvent
}
