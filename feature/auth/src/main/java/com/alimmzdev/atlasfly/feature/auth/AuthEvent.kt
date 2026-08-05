package com.alimmzdev.atlasfly.feature.auth

sealed interface AuthEvent {
    data object NavigateHome: AuthEvent
    data object NavigateSignupEmailVerification: AuthEvent
    data object ShowSignUpDialog: AuthEvent
}