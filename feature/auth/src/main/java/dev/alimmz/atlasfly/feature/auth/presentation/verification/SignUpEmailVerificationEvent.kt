package dev.alimmz.atlasfly.feature.auth.presentation.verification

sealed interface SignUpEmailVerificationEvent {
    data object NavigateHome : SignUpEmailVerificationEvent
}
