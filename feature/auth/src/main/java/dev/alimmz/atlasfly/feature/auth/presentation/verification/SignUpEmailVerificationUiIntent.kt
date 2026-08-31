package dev.alimmz.atlasfly.feature.auth.presentation.verification

sealed interface SignUpEmailVerificationUiIntent {
    data object ResendEmailClicked : SignUpEmailVerificationUiIntent
    data object CheckVerificationClicked : SignUpEmailVerificationUiIntent
    data object DismissMessage : SignUpEmailVerificationUiIntent
}
