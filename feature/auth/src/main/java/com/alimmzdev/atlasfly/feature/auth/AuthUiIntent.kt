package com.alimmzdev.atlasfly.feature.auth


sealed interface AuthUiIntent {
    data class EmailLogin(val email: String, val password: String) : AuthUiIntent
    data class GoogleLogin(val idToken: String) : AuthUiIntent
    data class GithubLogin(val accesToken: String) : AuthUiIntent
    data object RefreshTokens : AuthUiIntent
    data object Logout : AuthUiIntent
    data object DismissError : AuthUiIntent
}