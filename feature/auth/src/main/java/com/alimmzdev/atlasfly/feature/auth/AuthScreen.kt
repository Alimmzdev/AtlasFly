package com.alimmzdev.atlasfly.feature.auth

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import auth.model.AuthError
import com.alimmzdev.atlasfly.feature.auth.components.AccountNotFoundDialog
import com.alimmzdev.atlasfly.feature.auth.components.AuthHeader
import com.alimmzdev.atlasfly.feature.auth.components.AuthScreenScaffold
import com.alimmzdev.atlasfly.feature.auth.components.EmailField
import com.alimmzdev.atlasfly.feature.auth.components.GithubLoginButton
import com.alimmzdev.atlasfly.feature.auth.components.GoogleLoginButton
import com.alimmzdev.atlasfly.feature.auth.components.PasswordField
import com.alimmzdev.atlasfly.feature.auth.components.SignInButton
import com.alimmzdev.atlasfly.feature.auth.helpers.launchGitHubLogin
import com.alimmzdev.atlasfly.feature.auth.helpers.launchGoogleLogin
import kotlinx.coroutines.launch
import tech.nullexdev.atlasfly.core.designsystem.theme.AtlasFlyTheme

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
    onNavigateToHomeScreen: () -> Unit,
    onNavigateToSignUpEmailVerification: (String) -> Unit,
    serverClientId: String,
) {

    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when (it) {
                AuthEvent.NavigateHome -> onNavigateToHomeScreen()
                AuthEvent.ShowSignUpDialog -> showDialog = true
                AuthEvent.NavigateSignupEmailVerification -> onNavigateToSignUpEmailVerification(
                    uiState.email
                )
            }
        }
    }

    if (showDialog) {
        AccountNotFoundDialog(
            email = uiState.email,
            onConfirm = {
                showDialog = false
                viewModel.onIntent(
                    AuthUiIntent.EmailSignup(
                        email = uiState.email,
                        password = uiState.password
                    )
                )
            },
            onCancel = { showDialog = false }
        )
    }

    AuthScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onEmailPasswordLogin = {
            viewModel.onIntent(
                AuthUiIntent.EmailLogin(
                    email = uiState.email,
                    password = uiState.password
                )
            )
        },
        onGoogleLogin = {
            scope.launch {
                launchGoogleLogin(
                    activity = activity,
                    serverClientId = serverClientId,
                    onSuccess = { idToken ->
                        viewModel.onIntent(
                            AuthUiIntent.GoogleLogin(idToken)
                        )
                    },
                    onError = { e ->
                        if (e is NoCredentialException) {
                            openAddGoogleAccountScreen(activity = activity)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Google sign-in didn't go through. Try again?"
                                )
                            }
                        }
                    }
                )
            }
        },
        onGithubLogin = {
            launchGitHubLogin(
                activity = activity,
                onSuccess = { user, githubToken ->
                    if (githubToken != null) {
                        viewModel.onIntent(
                            AuthUiIntent.GithubLogin(user = user, accessToken = githubToken)
                        )
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Couldn't get a GitHub token. Try once more."
                            )
                        }
                    }
                },
                onFailure = { e ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = e.localizedMessage ?: "GitHub sign-in failed"
                        )
                    }
                }
            )
        },
    )
}

private fun openAddGoogleAccountScreen(activity: Activity) {
    val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
        putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
    }
    activity.startActivity(intent)
}


@Composable
private fun AuthScreenContent(
    uiState: AuthUiState,
    snackbarHostState: SnackbarHostState,
    onEmailPasswordLogin: () -> Unit,
    onIntent: (AuthUiIntent) -> Unit,
    onGoogleLogin: () -> Unit,
    onGithubLogin: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val socialEnabled = !uiState.isLoading

    AuthScreenScaffold(snackbarHostState = snackbarHostState) {
        AuthHeader()

        Spacer(modifier = Modifier.height(32.dp))

        EmailField(
            value = uiState.email,
            enabled = !uiState.isLoading,
            error = uiState.emailError,
            onValueChange = { onIntent(AuthUiIntent.EmailChanged(it)) },
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            value = uiState.password,
            error = uiState.passwordError,
            enabled = !uiState.isLoading,
            onValueChange = { onIntent(AuthUiIntent.PasswordChanged(it)) },
            onForgotPassword = { onIntent(AuthUiIntent.ForgotPasswordClicked) },
            onDone = {
                focusManager.clearFocus()
                onEmailPasswordLogin()
            }
        )

        AnimatedVisibility(visible = uiState.error != null) {
            Text(
                text = uiState.error?.toUserMessage() ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SignInButton(
            isEnable = uiState.signInButtonIsEnable,
            isLoading = uiState.isEmailPasswordLoading,
            onClick = onEmailPasswordLogin,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = "or",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp),
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            GoogleLoginButton(
                isLoading = uiState.isGoogleLoading,
                enabled = socialEnabled,
                onClick = onGoogleLogin,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(10.dp))
            GithubLoginButton(
                isLoading = uiState.isGithubLoading,
                enabled = socialEnabled,
                onClick = onGithubLogin,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "If you don't have an account yet, we'll set one up.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthScreenContentPreview() {
    AtlasFlyTheme {
        AuthScreenContent(
            uiState = AuthUiState(),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onEmailPasswordLogin = {},
            onGoogleLogin = {},
            onGithubLogin = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AuthScreenContentDarkPreview() {
    AtlasFlyTheme(darkTheme = true) {
        AuthScreenContent(
            uiState = AuthUiState(email = "maya@atlasfly.app", error = AuthError.InvalidCredentials),
            snackbarHostState = SnackbarHostState(),
            onIntent = {},
            onEmailPasswordLogin = {},
            onGoogleLogin = {},
            onGithubLogin = {},
        )
    }
}
