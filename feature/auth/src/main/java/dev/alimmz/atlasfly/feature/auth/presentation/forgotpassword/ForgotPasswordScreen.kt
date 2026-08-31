package dev.alimmz.atlasfly.feature.auth.presentation.forgotpassword

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import auth.model.AuthError
import dev.alimmz.atlasfly.core.designsystem.theme.AtlasFlyTheme
import dev.alimmz.atlasfly.feature.auth.presentation.components.AuthBackRow
import dev.alimmz.atlasfly.feature.auth.presentation.components.AuthMailMark
import dev.alimmz.atlasfly.feature.auth.presentation.components.AuthScreenScaffold
import dev.alimmz.atlasfly.feature.auth.presentation.components.EmailField
import dev.alimmz.atlasfly.feature.auth.presentation.components.SignInButton
import dev.alimmz.atlasfly.feature.auth.presentation.R

@Composable
fun ForgotPasswordScreen(
    email: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(email) {
        viewModel.seedEmail(email)
    }
    ForgotPasswordContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    onIntent: (ForgotPasswordUiIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    AuthScreenScaffold(modifier = modifier) {
        AuthBackRow(onBack = onBack)

        Spacer(modifier = Modifier.height(36.dp))

        if (uiState.sent) {
            AuthMailMark(modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.auth_check_inbox),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.auth_forgot_inbox_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = uiState.email,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
            uiState.message?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error.toForgotPasswordMessage(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
            SignInButton(
                isEnable = true,
                isLoading = false,
                text = stringResource(R.string.auth_forgot_open_email),
                onClick = { openEmailApp(context) },
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = { onIntent(ForgotPasswordUiIntent.ResendClicked) },
                enabled = !uiState.isLoading,
                modifier = Modifier.align(Alignment.Start),
            ) {
                Text(
                    text = stringResource(
                        if (uiState.isLoading) {
                            R.string.auth_forgot_sending
                        } else {
                            R.string.auth_verify_resend
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start),
            ) {
                Text(
                    text = stringResource(R.string.auth_forgot_back_signin),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.auth_forgot_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.auth_forgot_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(32.dp))
            EmailField(
                value = uiState.email,
                enabled = !uiState.isLoading,
                error = uiState.emailError?.let { stringResource(it) },
                onValueChange = { onIntent(ForgotPasswordUiIntent.EmailChanged(it)) },
                onDone = {
                    focusManager.clearFocus()
                    onIntent(ForgotPasswordUiIntent.SendResetClicked)
                },
            )
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error.toForgotPasswordMessage(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            SignInButton(
                isEnable = uiState.canSend,
                isLoading = uiState.isLoading,
                text = stringResource(R.string.auth_forgot_send),
                onClick = {
                    focusManager.clearFocus()
                    onIntent(ForgotPasswordUiIntent.SendResetClicked)
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start),
            ) {
                Text(
                    text = stringResource(R.string.auth_forgot_remembered),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AuthError.toForgotPasswordMessage(): String = stringResource(
    when (this) {
        AuthError.UserNotFound -> R.string.auth_forgot_error_user_not_found
        AuthError.InvalidCredentials -> R.string.auth_forgot_error_invalid
        AuthError.TooManyAttempts -> R.string.auth_forgot_error_too_many
        AuthError.NetworkError -> R.string.auth_forgot_error_network
        AuthError.AccountExistsDifferentProvider -> R.string.auth_forgot_error_provider
        else -> R.string.auth_forgot_error_generic
    }
)

private fun openEmailApp(context: android.content.Context) {
    val inbox = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_EMAIL)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(inbox)
    } catch (_: ActivityNotFoundException) {
        val chooser = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (_: ActivityNotFoundException) {
            Unit
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordFormPreview() {
    AtlasFlyTheme {
        ForgotPasswordContent(
            uiState = ForgotPasswordUiState(email = "maya@atlasfly.app"),
            onIntent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordSentPreview() {
    AtlasFlyTheme {
        ForgotPasswordContent(
            uiState = ForgotPasswordUiState(
                email = "maya@atlasfly.app",
                sent = true,
            ),
            onIntent = {},
            onBack = {},
        )
    }
}
