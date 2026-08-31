package dev.alimmz.atlasfly.feature.auth.presentation.verification

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
import dev.alimmz.atlasfly.feature.auth.presentation.R

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
                            message = R.string.auth_verify_email_sent,
                        )
                    }
                    is AuthResult.Failure -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error.toMessageRes(),
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
                        error = AuthError.EmailNotVerified.toMessageRes(),
                    )
                }
            }
        }
    }

    private fun AuthError.toMessageRes(): Int {
        return when (this) {
            AuthError.EmailNotVerified -> R.string.auth_verify_not_verified
            AuthError.TooManyAttempts -> R.string.auth_error_too_many
            AuthError.NetworkError -> R.string.auth_error_network
            else -> R.string.auth_error_unknown
        }
    }
}
