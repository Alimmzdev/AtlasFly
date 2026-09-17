package dev.alimmz.atlasfly.feature.auth.presentation.login

sealed interface LoginUiIntent {
    data class EmailLogin(val email: String, val password: String) : LoginUiIntent
    data class EmailSignup(val email: String, val password: String) : LoginUiIntent
    data class EmailChanged(val value: String) : LoginUiIntent
    data class PasswordChanged(val value: String) : LoginUiIntent
    data object GoogleLogin : LoginUiIntent
    data object GithubLogin : LoginUiIntent
    data object RefreshTokens : LoginUiIntent
    data object Logout : LoginUiIntent
    data object DismissError : LoginUiIntent
    data object ForgotPasswordClicked : LoginUiIntent
    data object NavigateToSignUp : LoginUiIntent
}
