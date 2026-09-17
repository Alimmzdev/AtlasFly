package dev.alimmz.atlasfly.feature.auth.presentation.verification

sealed interface SignUpEmailVerificationUiIntent {
    data class ResendEmailClicked(val email: String) : SignUpEmailVerificationUiIntent
    data object CheckVerificationClicked : SignUpEmailVerificationUiIntent
    data object DismissMessage : SignUpEmailVerificationUiIntent
}
