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
import com.alimmzdev.atlasfly.feature.auth.AuthScreen
import com.alimmzdev.atlasfly.feature.auth.SignUpEmailVerificationScreen
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
            backStack.clear()
            backStack.add(route)
            viewModel.onEvent(AtlasFlyEvent.DeepLinkHandled)
        }
    }
    LaunchedEffect(uiState.isAuthorized) {
        if (uiState.isAuthorized && backStack.lastOrNull() !is Routes.Home) {
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
                navEntry(key, onNavigate = { route ->
                    backStack.add(route)
                }, viewModel = viewModel)
            }
        )
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        uiState.errorMessage?.let { errorMessage ->
            Text(
                text = errorMessage,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        uiState.emailVerificationMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private fun navEntry(
    key: Routes,
    onNavigate: (route: Routes) -> Unit,
    viewModel: AtlasFlyViewModel,
): NavEntry<Routes> {
    return when (key) {
        Routes.Auth.Login -> NavEntry(key) {
            AuthScreen(
                serverClientId = stringResource(R.string.default_web_client_id),
                onNavigateToHomeScreen = {
                    onNavigate(Routes.Home)
                },
                onNavigateToSignUpEmailVerification = { email ->
                    onNavigate(Routes.Auth.SignUpEmailVerification(email))
                }
            )
        }

        is Routes.Auth.SignUpEmailVerification -> NavEntry(key) {
            SignUpEmailVerificationScreen(
                email = key.email,
                onNavigateToHome = {
                    viewModel.onEvent(AtlasFlyEvent.Refresh)
                    onNavigate(Routes.Home)
                }
            )
        }

        Routes.Auth.ForgotPassword -> NavEntry(key) {
            Text("Forgot Password Screen")
        }

        Routes.Home -> NavEntry(key) {
            Text("Home Screen")
        }

        Routes.Flights -> NavEntry(key) {
            Text("Flights Screen")
        }

        Routes.Profile -> NavEntry(key) {
            Text("Profile Screen")
        }
    }
}
