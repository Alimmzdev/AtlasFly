package dev.alimmz.atlasfly.app

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dev.alimmz.atlasfly.feature.auth.presentation.forgotpassword.ForgotPasswordScreen
import dev.alimmz.atlasfly.feature.auth.presentation.login.LoginScreen
import dev.alimmz.atlasfly.feature.auth.presentation.resetpassword.ResetPasswordScreen
import dev.alimmz.atlasfly.feature.auth.presentation.verification.SignUpEmailVerificationScreen
import dev.alimmz.atlasfly.R
import dev.alimmz.atlasfly.core.navigation.Routes

@Composable
fun AtlasFlyApp(
    deepLinkUri: Uri? = null,
    viewModel: AtlasFlyViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = remember {
        mutableStateListOf(if (uiState.isAuthorized) Routes.Home else Routes.Auth.Login)
    }
    LaunchedEffect(deepLinkUri) {
        deepLinkUri?.let { uri ->
            viewModel.onEvent(AtlasFlyEvent.HandleDeepLink(uri))
        }
    }
    LaunchedEffect(uiState.pendingNavigation) {
        uiState.pendingNavigation?.let { route ->
            when (route) {
                is Routes.Auth.ResetPassword -> {
                    backStack.clear()
                    backStack.add(Routes.Auth.Login)
                    backStack.add(route)
                }
                is Routes.Auth.ForgotPassword -> {
                    backStack.clear()
                    backStack.add(Routes.Auth.Login)
                    backStack.add(route)
                }
                else -> {
                    backStack.clear()
                    backStack.add(route)
                }
            }
            viewModel.onEvent(AtlasFlyEvent.DeepLinkHandled)
        }
    }
    LaunchedEffect(uiState.isAuthorized) {
        val top = backStack.lastOrNull()
        val stayingOnAuth = top is Routes.Auth.ResetPassword || top is Routes.Auth.ForgotPassword
        if (uiState.isAuthorized && top !is Routes.Home && !stayingOnAuth) {
            backStack.clear()
            backStack.add(Routes.Home)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },
            entryProvider = { key ->
                navEntry(
                    key = key,
                    onNavigate = { route -> backStack.add(route) },
                    onBack = { backStack.removeLastOrNull() },
                    viewModel = viewModel,
                )
            }
        )
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        uiState.errorMessage?.let { errorMessage ->
            Text(
                text = stringResource(errorMessage),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        uiState.emailVerificationMessage?.let { message ->
            Text(
                text = stringResource(message),
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private fun navEntry(
    key: Routes,
    onNavigate: (route: Routes) -> Unit,
    onBack: () -> Unit,
    viewModel: AtlasFlyViewModel,
): NavEntry<Routes> {
    return when (key) {
        Routes.Auth.Login -> NavEntry(key) {
            LoginScreen(
                serverClientId = stringResource(R.string.default_web_client_id),
                onNavigateToHomeScreen = {
                    onNavigate(Routes.Home)
                },
                onNavigateToSignUpEmailVerification = { email ->
                    onNavigate(Routes.Auth.SignUpEmailVerification(email))
                },
                onNavigateToForgotPassword = { email ->
                    onNavigate(Routes.Auth.ForgotPassword(email))
                },
            )
        }

        is Routes.Auth.SignUpEmailVerification -> NavEntry(key) {
            SignUpEmailVerificationScreen(
                email = key.email,
                onNavigateToHome = {
                    viewModel.onEvent(AtlasFlyEvent.Refresh)
                    onNavigate(Routes.Home)
                },
            )
        }

        is Routes.Auth.ForgotPassword -> NavEntry(key) {
            ForgotPasswordScreen(
                email = key.email,
                onBack = onBack,
            )
        }

        is Routes.Auth.ResetPassword -> NavEntry(key) {
            ResetPasswordScreen(
                oobCode = key.oobCode,
                onBackToLogin = onBack,
                onRequestNewLink = {
                    onBack()
                    onNavigate(Routes.Auth.ForgotPassword())
                },
            )
        }

        Routes.Home -> NavEntry(key) {
            Text(stringResource(R.string.home_screen))
        }

        Routes.Flights -> NavEntry(key) {
            Text(stringResource(R.string.flights_screen))
        }

        Routes.Profile -> NavEntry(key) {
            Text(stringResource(R.string.profile_screen))
        }
    }
}
