package com.alimmzdev.atlasfly.feature.auth

import com.google.firebase.auth.FirebaseUser


sealed interface AuthUiIntent {
    data class EmailLogin(val email: String, val password: String) : AuthUiIntent
    data class EmailSignup(val email: String, val password: String) : AuthUiIntent
    data class EmailChanged(val value: String) : AuthUiIntent
    data class PasswordChanged(val value: String) : AuthUiIntent
    data class GoogleLogin(val idToken: String) : AuthUiIntent
    data class GithubLogin(val user: FirebaseUser, val accessToken: String) : AuthUiIntent
    data object RefreshTokens : AuthUiIntent
    data object Logout : AuthUiIntent
    data object DismissError : AuthUiIntent
    data object ForgotPasswordClicked : AuthUiIntent
    data object NavigateToSignUp : AuthUiIntent
}