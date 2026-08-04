package com.alimmzdev.atlasfly.feature.auth

import auth.model.AuthError

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: AuthError? = null,
)