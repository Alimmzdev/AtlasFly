package dev.alimmz.atlasfly.feature.auth.presentation.resetpassword

sealed interface ResetPasswordUiIntent {
    data class PasswordChanged(val value: String) : ResetPasswordUiIntent
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordUiIntent
    data object SubmitClicked : ResetPasswordUiIntent
}
