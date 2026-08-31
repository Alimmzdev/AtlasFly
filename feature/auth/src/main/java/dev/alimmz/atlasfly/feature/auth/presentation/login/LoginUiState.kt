package dev.alimmz.atlasfly.feature.auth.presentation.login

import androidx.annotation.StringRes
import auth.model.AuthError
import auth.model.AuthProvider

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = false,
    val loadingProvider: AuthProvider? = null,
    val error: AuthError? = null,
) {
    val signInButtonIsEnable
        get() = !isLoading && email.isNotBlank() && password.isNotBlank()

    val isEmailPasswordLoading
        get() = isLoading && loadingProvider is AuthProvider.EmailPassword

    val isGoogleLoading
        get() = isLoading && loadingProvider is AuthProvider.Google

    val isGithubLoading
        get() = isLoading && loadingProvider is AuthProvider.Github
}
