package com.alimmzdev.atlasfly.feature.auth

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.alimmzdev.atlasfly.feature.auth.login.launchGoogleLogin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.launch

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
                    onSuccess = { idToken -> viewModel.onIntent(
                        AuthUiIntent.GoogleLogin(idToken)
                    ) },
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
        putExtra(android.provider.Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Button(
            onClick = onGoogleLogin,
            enabled = !uiState.isLoading
        ) {
            Text("Continue with Google")
        }


        Button(
            onClick = onGithubLogin,
            enabled = !uiState.isLoading
        ) {
            Text("Continue with GitHub")
        }


        uiState.error?.let { error ->
            Text(error.toString())
        }
    }
}