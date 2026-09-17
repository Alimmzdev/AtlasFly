package dev.alimmz.atlasfly.feature.explore.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.alimmz.atlasfly.core.presentation.components.FeaturePlaceholderScreen
import dev.alimmz.atlasfly.core.presentation.shell.MainSharedViewModel

@Composable
fun ExploreScreen(
    sharedViewModel: MainSharedViewModel,
    modifier: Modifier = Modifier,
) {
    FeaturePlaceholderScreen(
        titleRes = R.string.explore_title,
        icon = Icons.Outlined.Explore,
        sharedViewModel = sharedViewModel,
        modifier = modifier,
    )
}
