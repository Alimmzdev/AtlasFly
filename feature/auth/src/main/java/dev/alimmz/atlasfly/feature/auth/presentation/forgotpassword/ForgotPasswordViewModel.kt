package dev.alimmz.atlasfly.feature.auth.presentation.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.model.AuthError
import auth.model.AuthResult
import auth.usecase.SendPasswordResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.alimmz.atlasfly.feature.auth.presentation.R

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun seedEmail(email: String) {
        if (_uiState.value.email.isBlank() && email.isNotBlank()) {
            _uiState.update { it.copy(email = email.trim()) }
        }
    }

    fun onIntent(intent: ForgotPasswordUiIntent) {
        when (intent) {
            is ForgotPasswordUiIntent.EmailChanged -> _uiState.update {
                it.copy(email = intent.value, emailError = null, error = null)
            }
            ForgotPasswordUiIntent.SendResetClicked -> sendReset()
            ForgotPasswordUiIntent.ResendClicked -> sendReset()
        }
    }

    private fun sendReset() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = R.string.auth_forgot_email_blank) }
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            _uiState.update { it.copy(emailError = R.string.auth_forgot_email_invalid) }
            return
        }
        viewModelScope.launch {
            sendPasswordResetEmailUseCase(email).collect { result ->
                when (result) {
                    is AuthResult.Loading -> _uiState.update {
                        it.copy(isLoading = true, error = null, emailError = null)
                    }
                    is AuthResult.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            sent = true,
                            message = if (it.sent) R.string.auth_forgot_resent else null,
                        )
                    }
                    is AuthResult.Failure -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.error.takeIf { error ->
                                error !is AuthError.Cancelled
                            },
                        )
                    }
                }
            }
        }
    }
}
