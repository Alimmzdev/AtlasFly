package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.alimmz.atlasfly.core.presentation.components.FeatureDetailScreen

@Composable
fun SavedPlacesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeatureDetailScreen(
        titleRes = R.string.profile_saved_places,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun SavedTripsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeatureDetailScreen(
        titleRes = R.string.profile_saved_trips,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun PreferencesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeatureDetailScreen(
        titleRes = R.string.profile_preferences,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeatureDetailScreen(
        titleRes = R.string.profile_account_settings,
        onBack = onBack,
        modifier = modifier,
    )
}
