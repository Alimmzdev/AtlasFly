package dev.alimmz.atlasfly.feature.auth.presentation.resetpassword

import androidx.annotation.StringRes
import auth.model.AuthError

data class ResetPasswordUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    val isVerifyingCode: Boolean = true,
    val isLoading: Boolean = false,
    val codeValid: Boolean = false,
    val updated: Boolean = false,
    val error: AuthError? = null,
) {
    val canSubmit: Boolean
        get() = !isLoading && codeValid && password.isNotBlank() && confirmPassword.isNotBlank()
}
