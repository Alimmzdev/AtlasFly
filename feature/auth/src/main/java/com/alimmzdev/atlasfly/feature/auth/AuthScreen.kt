package com.alimmzdev.atlasfly.feature.auth

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import auth.model.AuthProvider
import com.alimmzdev.atlasfly.feature.auth.login.launchGoogleLogin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.launch
import tech.nullexdev.atlasfly.feature.auth.presentation.R

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
    serverClientId: String,
) {

    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()


    AuthScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
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
                            // generic error UI
                        }
                    }
                )
            }
        },
        onGithubLogin = {
            val provider = OAuthProvider.newBuilder("github.com")
            // Optional: request extra GitHub scopes, e.g. "read:user"
            provider.scopes = listOf("read:user")

            val auth = FirebaseAuth.getInstance()
            val pending = auth.pendingAuthResult

            if (pending != null) {
                // There's already a pending sign-in, continue it
                pending
                    .addOnSuccessListener { result ->
                        // result.user is now signed in
                    }
                    .addOnFailureListener { e ->
                        // handle failure, e.g. update uiState.errorMessage
                    }
            } else {
                auth.startActivityForSignInWithProvider(activity, provider.build())
                    .addOnSuccessListener { result ->
                        // result.user is now signed in
                    }
                    .addOnFailureListener { e ->
                        // handle failure
                    }
            }
        }
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
    onIntent: (AuthUiIntent) -> Unit,
    onGoogleLogin: () -> Unit,
    onGithubLogin: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Welcome back",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            "Sign in to continue planning your trips",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- Email field ---
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onIntent(AuthUiIntent.EmailChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it) } },
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Password field ---
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onIntent(AuthUiIntent.PasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let { { Text(it) } },
            enabled = !uiState.isLoading,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onIntent(AuthUiIntent.EmailLogin(email = uiState.email, password = uiState.password))
            })
        )

        // Forgot password — right-aligned, close to the field it relates to
        TextButton(
            onClick = { onIntent(AuthUiIntent.ForgotPasswordClicked) },
            modifier = Modifier.align(Alignment.End),
            enabled = !uiState.isLoading,
        ) {
            Text("Forgot password?")
        }

        AnimatedVisibility(visible = uiState.error != null) {
            Text(
                text = uiState.error?.toUserMessage() ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onIntent(AuthUiIntent.EmailLogin(email = uiState.email, password = uiState.password)) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank(),
        ) {
            if (uiState.isLoading && uiState.loadingProvider is AuthProvider.EmailPassword) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current
                )
            } else {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                "  or continue with  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            onClick = onGoogleLogin,
            enabled = !uiState.isLoading,
        ) {
            if (uiState.isLoading && uiState.loadingProvider is AuthProvider.Google) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Image(
                    painter = painterResource(R.drawable.google_icon_logo),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Continue with Google")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
            onClick = onGithubLogin,
            enabled = !uiState.isLoading,
        ) {
            if (uiState.isLoading && uiState.loadingProvider is AuthProvider.Github) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Image(
                    painter = painterResource(R.drawable.github_invertocat_white),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Continue with GitHub")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Sign up link ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                "Don't have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Sign up",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                    onIntent(AuthUiIntent.NavigateToSignUp)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun AuthScreenContentPreview() {
    AuthScreenContent(
        uiState = AuthUiState(),
        onIntent = {},
        onGoogleLogin = {},
        onGithubLogin = {},
    )
}