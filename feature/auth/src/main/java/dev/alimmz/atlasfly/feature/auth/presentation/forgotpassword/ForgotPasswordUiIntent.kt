package dev.alimmz.atlasfly.feature.auth.presentation.forgotpassword

sealed interface ForgotPasswordUiIntent {
    data class EmailChanged(val value: String) : ForgotPasswordUiIntent
    data object SendResetClicked : ForgotPasswordUiIntent
    data object ResendClicked : ForgotPasswordUiIntent
}
