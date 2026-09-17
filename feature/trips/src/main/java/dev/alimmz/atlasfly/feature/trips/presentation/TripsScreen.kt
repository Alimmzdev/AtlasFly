package dev.alimmz.atlasfly.feature.trips.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.alimmz.atlasfly.core.presentation.components.FeaturePlaceholderScreen
import dev.alimmz.atlasfly.core.presentation.shell.MainSharedViewModel

@Composable
fun TripsScreen(
    sharedViewModel: MainSharedViewModel,
    modifier: Modifier = Modifier,
) {
    FeaturePlaceholderScreen(
        titleRes = R.string.trips_title,
        icon = Icons.Outlined.Luggage,
        sharedViewModel = sharedViewModel,
        modifier = modifier,
    )
}
