package dev.alimmz.atlasfly.feature.auth.presentation.mapper

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import auth.model.AuthError
import dev.alimmz.atlasfly.feature.auth.presentation.R

@StringRes
internal fun AuthError.toStringRes(): Int? = when (this) {
    AuthError.InvalidCredentials -> R.string.auth_error_invalid_credentials
    AuthError.UserNotFound -> R.string.auth_error_user_not_found
    AuthError.AccountExistsDifferentProvider -> R.string.auth_error_different_provider
    AuthError.InvalidActionCode -> R.string.auth_error_invalid_action_code
    AuthError.EmailNotVerified -> R.string.auth_error_email_not_verified
    AuthError.WeakPassword -> R.string.auth_error_weak_password
    AuthError.NetworkError -> R.string.auth_error_network
    AuthError.TooManyAttempts -> R.string.auth_error_too_many
    AuthError.Cancelled -> null
    is AuthError.Unknown -> R.string.auth_error_unknown
}

@Composable
internal fun AuthError.toUserMessage(): String =
    toStringRes()?.let { stringResource(it) }.orEmpty()
