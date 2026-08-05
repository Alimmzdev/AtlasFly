package com.alimmzdev.atlasfly.feature.auth

import auth.model.AuthError
import auth.model.AuthProvider

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
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

fun AuthError.toUserMessage(): String = when (this) {
    AuthError.InvalidCredentials -> "Incorrect email or password"
    AuthError.UserNotFound -> "No account found with this email"
    AuthError.AccountExistsDifferentProvider -> "This email is linked to a different sign-in method"
    AuthError.NetworkError -> "Check your connection and try again"
    AuthError.TooManyAttempts -> "Too many attempts. Try again later"
    AuthError.Cancelled -> "" // user-initiated cancel, don't show an error at all
    is AuthError.Unknown -> "Something went wrong. Please try again"
}