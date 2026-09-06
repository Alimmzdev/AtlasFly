package dev.alimmz.atlasfly.app

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dev.alimmz.atlasfly.app.main.MainDestination
import dev.alimmz.atlasfly.app.main.MainShell
import dev.alimmz.atlasfly.feature.auth.presentation.forgotpassword.ForgotPasswordScreen
import dev.alimmz.atlasfly.feature.auth.presentation.login.LoginScreen
import dev.alimmz.atlasfly.feature.auth.presentation.resetpassword.ResetPasswordScreen
import dev.alimmz.atlasfly.feature.auth.presentation.verification.SignUpEmailVerificationScreen
import dev.alimmz.atlasfly.R
import dev.alimmz.atlasfly.core.navigation.Routes
import dev.alimmz.atlasfly.core.presentation.shell.MainSharedViewModel

private val START_DESTINATION: Routes.Main.TopLevel = Routes.Main.TopLevel.Home

@Composable
fun AtlasFlyApp(
    deepLinkUri: Uri? = null,
    viewModel: AtlasFlyViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Keep composition empty while splash covers auth bootstrap.
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val backStack = rememberSaveable(
        saver = Saver(
            save = { Json.encodeToString(ListSerializer(Routes.serializer()), it.toList()) },
            restore = {
                Json.decodeFromString(ListSerializer(Routes.serializer()), it).toMutableStateList()
            },
        ),
    ) {
        mutableStateListOf<Routes>(
            when {
                uiState.isAuthorized -> START_DESTINATION
                uiState.pendingNavigation != null -> uiState.pendingNavigation!!
                else -> Routes.Auth.Login
            }
        )
    }
    val mainSharedViewModel: MainSharedViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (uiState.pendingNavigation != null) {
            viewModel.onEvent(AtlasFlyEvent.DeepLinkHandled)
        }
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
                is Routes.Auth.SignUpEmailVerification -> {
                    backStack.clear()
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
        if (uiState.isAuthorized && top !is Routes.Main && !stayingOnAuth) {
            backStack.clear()
            backStack.add(START_DESTINATION)
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
                    onSelectTab = { tab ->
                        // Tabs are siblings: switching replaces the main stack
                        // instead of stacking destinations on top of each other.
                        if (backStack.lastOrNull() != tab) {
                            backStack.clear()
                            backStack.add(tab)
                        }
                    },
                    sharedViewModel = mainSharedViewModel,
                    snackbarHostState = snackbarHostState,
                    viewModel = viewModel,
                )
            }
        )
        if (backStack.lastOrNull() is Routes.Auth) {
            LanguageSwitcher(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 16.dp, top = 8.dp),
            )
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
    onSelectTab: (tab: Routes.Main.TopLevel) -> Unit,
    sharedViewModel: MainSharedViewModel,
    snackbarHostState: SnackbarHostState,
    viewModel: AtlasFlyViewModel,
): NavEntry<Routes> {
    return when (key) {
        Routes.Auth.Login -> NavEntry(key) {
            LoginScreen(
                serverClientId = stringResource(R.string.default_web_client_id),
                onNavigateToHomeScreen = {
                    onNavigate(START_DESTINATION)
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
                    onNavigate(START_DESTINATION)
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

        is Routes.Main -> NavEntry(key) {
            MainShell(
                route = key,
                sharedViewModel = sharedViewModel,
                snackbarHostState = snackbarHostState,
                onSelectTab = onSelectTab,
            ) {
                MainDestination(
                    route = key,
                    sharedViewModel = sharedViewModel,
                    onNavigate = onNavigate,
                    onBack = onBack,
                    onLogout = { viewModel.onEvent(AtlasFlyEvent.Logout) },
                )
            }
        }
    }
}
