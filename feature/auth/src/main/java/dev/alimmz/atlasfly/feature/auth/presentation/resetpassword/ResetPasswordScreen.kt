package dev.alimmz.atlasfly.feature.auth.presentation.resetpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import auth.model.AuthError
import dev.alimmz.atlasfly.core.designsystem.theme.AtlasFlyTheme
import dev.alimmz.atlasfly.feature.auth.presentation.components.AuthBackRow
import dev.alimmz.atlasfly.feature.auth.presentation.components.AuthScreenScaffold
import dev.alimmz.atlasfly.feature.auth.presentation.components.PasswordField
import dev.alimmz.atlasfly.feature.auth.presentation.components.SignInButton
import dev.alimmz.atlasfly.feature.auth.presentation.R

@Composable
fun ResetPasswordScreen(
    oobCode: String,
    onBackToLogin: () -> Unit,
    onRequestNewLink: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(oobCode) {
        viewModel.start(oobCode)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ResetPasswordEvent.PasswordUpdated -> Unit
            }
        }
    }
    ResetPasswordContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBackToLogin = onBackToLogin,
        onRequestNewLink = onRequestNewLink,
        modifier = modifier,
    )
}

@Composable
private fun ResetPasswordContent(
    uiState: ResetPasswordUiState,
    onIntent: (ResetPasswordUiIntent) -> Unit,
    onBackToLogin: () -> Unit,
    onRequestNewLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    AuthScreenScaffold(modifier = modifier) {
        AuthBackRow(onBack = onBackToLogin)

        Spacer(modifier = Modifier.height(36.dp))

        when {
            uiState.isVerifyingCode -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.CenterHorizontally),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            uiState.updated -> {
                Text(
                    text = stringResource(R.string.auth_reset_success_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.auth_reset_success_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(32.dp))
                SignInButton(
                    isEnable = true,
                    isLoading = false,
                    text = stringResource(R.string.auth_reset_signin),
                    onClick = onBackToLogin,
                )
            }

            !uiState.codeValid -> {
                Text(
                    text = stringResource(R.string.auth_reset_expired_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.error.toResetPasswordMessage()
                        ?: stringResource(R.string.auth_reset_expired_fallback),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(32.dp))
                SignInButton(
                    isEnable = true,
                    isLoading = false,
                    text = stringResource(R.string.auth_reset_new_link),
                    onClick = onRequestNewLink,
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.align(Alignment.Start),
                ) {
                    Text(
                        text = stringResource(R.string.auth_forgot_back_signin),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                Text(
                    text = stringResource(R.string.auth_reset_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.auth_reset_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (uiState.email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = uiState.email,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                PasswordField(
                    value = uiState.password,
                    error = uiState.passwordError?.let { stringResource(it) },
                    enabled = !uiState.isLoading,
                    label = stringResource(R.string.auth_reset_new_password),
                    placeholder = stringResource(R.string.auth_reset_new_password),
                    imeAction = ImeAction.Next,
                    onValueChange = { onIntent(ResetPasswordUiIntent.PasswordChanged(it)) },
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = { focusManager.moveFocus(FocusDirection.Down) },
                )
                Spacer(modifier = Modifier.height(16.dp))
                PasswordField(
                    value = uiState.confirmPassword,
                    error = uiState.confirmPasswordError?.let { stringResource(it) },
                    enabled = !uiState.isLoading,
                    label = stringResource(R.string.auth_reset_confirm),
                    placeholder = stringResource(R.string.auth_reset_confirm_placeholder),
                    onValueChange = { onIntent(ResetPasswordUiIntent.ConfirmPasswordChanged(it)) },
                    onDone = {
                        focusManager.clearFocus()
                        onIntent(ResetPasswordUiIntent.SubmitClicked)
                    },
                )
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error.toResetPasswordMessage().orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                SignInButton(
                    isEnable = uiState.canSubmit,
                    isLoading = uiState.isLoading,
                    text = stringResource(R.string.auth_reset_update),
                    onClick = {
                        focusManager.clearFocus()
                        onIntent(ResetPasswordUiIntent.SubmitClicked)
                    },
                )
            }
        }
    }
}

@Composable
private fun AuthError?.toResetPasswordMessage(): String? = when (this) {
    null -> null
    AuthError.InvalidActionCode -> stringResource(R.string.auth_reset_error_invalid_code)
    AuthError.WeakPassword -> stringResource(R.string.auth_error_weak_password)
    AuthError.NetworkError -> stringResource(R.string.auth_error_network)
    AuthError.TooManyAttempts -> stringResource(R.string.auth_error_too_many)
    else -> stringResource(R.string.auth_reset_error_generic)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResetPasswordFormPreview() {
    AtlasFlyTheme {
        ResetPasswordContent(
            uiState = ResetPasswordUiState(
                email = "maya@atlasfly.app",
                isVerifyingCode = false,
                codeValid = true,
            ),
            onIntent = {},
            onBackToLogin = {},
            onRequestNewLink = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResetPasswordExpiredPreview() {
    AtlasFlyTheme {
        ResetPasswordContent(
            uiState = ResetPasswordUiState(
                isVerifyingCode = false,
                codeValid = false,
                error = AuthError.InvalidActionCode,
            ),
            onIntent = {},
            onBackToLogin = {},
            onRequestNewLink = {},
        )
    }
}
