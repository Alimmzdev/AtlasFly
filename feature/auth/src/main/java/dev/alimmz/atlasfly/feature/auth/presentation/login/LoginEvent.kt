package dev.alimmz.atlasfly.feature.auth.presentation.login

sealed interface LoginEvent {
    data object NavigateHome : LoginEvent
    data class NavigateSignupEmailVerification(val email: String) : LoginEvent
    data object ShowSignUpDialog : LoginEvent
    data class NavigateForgotPassword(val email: String) : LoginEvent
}
