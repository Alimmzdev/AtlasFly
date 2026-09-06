package dev.alimmz.atlasfly.feature.auth.presentation.resetpassword

sealed interface ResetPasswordEvent {
    data object PasswordUpdated : ResetPasswordEvent
}
