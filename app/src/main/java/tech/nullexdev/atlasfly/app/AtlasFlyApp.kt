package tech.nullexdev.atlasfly.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.alimmzdev.atlasfly.feature.auth.AuthScreen
import tech.nullexdev.atlasfly.R
import tech.nullexdev.atlasfly.core.navigation.Routes

@Composable
fun AtlasFlyApp(
    viewModel: AtlasFlyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = remember {
        mutableStateListOf( if (uiState.isAuthorized) Routes.Home else Routes.Auth.Login)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },
            entryProvider = { key ->
                navEntry(key)
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
    }
}

private fun navEntry(key: Any): NavEntry<Any> {
    return when (key) {
        Routes.Auth.Login -> NavEntry(key) {
            AuthScreen(
                serverClientId = stringResource(R.string.default_web_client_id)
            )
        }
        Routes.Auth.SignUp -> NavEntry(key) {
            Text("Sign Up Screen")
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
        else -> error("Unknown route: $key")
    }
}
