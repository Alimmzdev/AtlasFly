package dev.alimmz.atlasfly.feature.auth.presentation.login

sealed interface LoginEvent {
    data object NavigateHome : LoginEvent
    data object NavigateSignupEmailVerification : LoginEvent
    data object ShowSignUpDialog : LoginEvent
    data class NavigateForgotPassword(val email: String) : LoginEvent
}
