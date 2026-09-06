package dev.alimmz.atlasfly.feature.home.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.alimmz.atlasfly.core.presentation.components.FeaturePlaceholderScreen
import dev.alimmz.atlasfly.core.presentation.shell.MainSharedViewModel

@Composable
fun HomeScreen(
    sharedViewModel: MainSharedViewModel,
    modifier: Modifier = Modifier,
) {
    FeaturePlaceholderScreen(
        titleRes = R.string.home_title,
        icon = Icons.Outlined.Home,
        sharedViewModel = sharedViewModel,
        modifier = modifier,
    )
}
