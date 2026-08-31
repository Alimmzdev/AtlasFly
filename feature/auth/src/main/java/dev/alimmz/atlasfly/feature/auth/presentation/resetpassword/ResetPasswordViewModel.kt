package dev.alimmz.atlasfly.feature.auth.presentation.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import auth.model.AuthError
import auth.model.AuthResult
import auth.model.ResetCodeResult
import auth.usecase.ConfirmPasswordResetUseCase
import auth.usecase.VerifyPasswordResetCodeUseCase
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
class ResetPasswordViewModel @Inject constructor(
    private val verifyPasswordResetCodeUseCase: VerifyPasswordResetCodeUseCase,
    private val confirmPasswordResetUseCase: ConfirmPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<ResetPasswordEvent>()
    val events = _events.receiveAsFlow()

    private var oobCode: String = ""
    private var started: Boolean = false

    fun start(oobCode: String) {
        if (started) return
        started = true
        this.oobCode = oobCode
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingCode = true, error = null) }
            when (val result = verifyPasswordResetCodeUseCase(oobCode)) {
                is ResetCodeResult.Valid -> _uiState.update {
                    it.copy(
                        isVerifyingCode = false,
                        email = result.email,
                        codeValid = true,
                    )
                }
                is ResetCodeResult.Invalid -> _uiState.update {
                    it.copy(
                        isVerifyingCode = false,
                        codeValid = false,
                        error = result.error,
                    )
                }
            }
        }
    }

    fun onIntent(intent: ResetPasswordUiIntent) {
        when (intent) {
            is ResetPasswordUiIntent.PasswordChanged -> _uiState.update {
                it.copy(password = intent.value, passwordError = null, error = null)
            }
            is ResetPasswordUiIntent.ConfirmPasswordChanged -> _uiState.update {
                it.copy(confirmPassword = intent.value, confirmPasswordError = null, error = null)
            }
            ResetPasswordUiIntent.SubmitClicked -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        val passwordError = when {
            state.password.isBlank() -> R.string.auth_reset_error_blank
            state.password.length < 6 -> R.string.auth_reset_error_weak
            else -> null
        }
        val confirmError = when {
            state.confirmPassword.isBlank() -> R.string.auth_reset_error_confirm_blank
            state.confirmPassword != state.password -> R.string.auth_reset_error_mismatch
            else -> null
        }
        if (passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(passwordError = passwordError, confirmPasswordError = confirmError)
            }
            return
        }
        viewModelScope.launch {
            confirmPasswordResetUseCase(oobCode, state.password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> _uiState.update {
                        it.copy(isLoading = true, error = null)
                    }
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, updated = true) }
                        _events.send(ResetPasswordEvent.PasswordUpdated)
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
