package dev.alimmz.atlasfly.feature.planner.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.alimmz.atlasfly.core.presentation.components.FeaturePlaceholderScreen
import dev.alimmz.atlasfly.core.presentation.shell.MainSharedViewModel

@Composable
fun PlannerScreen(
    sharedViewModel: MainSharedViewModel,
    modifier: Modifier = Modifier,
) {
    FeaturePlaceholderScreen(
        titleRes = R.string.planner_title,
        icon = Icons.Outlined.AddCircleOutline,
        sharedViewModel = sharedViewModel,
        modifier = modifier,
    )
}
