package dev.alimmz.atlasfly.feature.auth.presentation.forgotpassword

import androidx.annotation.StringRes
import auth.model.AuthError

data class ForgotPasswordUiState(
    val email: String = "",
    @StringRes val emailError: Int? = null,
    val isLoading: Boolean = false,
    val sent: Boolean = false,
    @StringRes val message: Int? = null,
    val error: AuthError? = null,
) {
    val canSend: Boolean
        get() = !isLoading && email.isNotBlank()
}
