package dev.alimmz.atlasfly.app.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.alimmz.atlasfly.core.navigation.Routes
import dev.alimmz.atlasfly.core.presentation.shell.MainSharedViewModel

/**
 * Chrome shared by every [Routes.Main] destination.
 *
 * The bottom navigation bar is only shown for top-level destinations, so detail
 * destinations pushed on top of a tab get the full screen. [sharedViewModel] and
 * [snackbarHostState] are owned above the navigation display so they outlive the
 * individual destinations.
 */
@Composable
fun MainShell(
    route: Routes.Main,
    sharedViewModel: MainSharedViewModel,
    snackbarHostState: SnackbarHostState,
    onSelectTab: (Routes.Main.TopLevel) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val sharedUiState by sharedViewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab = MainTab.fromRoute(route)

    sharedUiState.message?.let { message ->
        val text = stringResource(message)
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(text)
            sharedViewModel.onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (selectedTab != null) {
                MainBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab -> onSelectTab(tab.route) },
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            content()
        }
    }
}
