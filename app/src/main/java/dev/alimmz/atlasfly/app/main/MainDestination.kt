package dev.alimmz.atlasfly.app.main

import androidx.compose.runtime.Composable
import dev.alimmz.atlasfly.app.LanguageSwitcher
import dev.alimmz.atlasfly.core.navigation.Routes
import dev.alimmz.atlasfly.core.presentation.shell.MainSharedViewModel
import dev.alimmz.atlasfly.feature.explore.presentation.ExploreScreen
import dev.alimmz.atlasfly.feature.home.presentation.HomeScreen
import dev.alimmz.atlasfly.feature.planner.presentation.PlannerScreen
import dev.alimmz.atlasfly.feature.profile.presentation.AccountSettingsScreen
import dev.alimmz.atlasfly.feature.profile.presentation.PreferencesScreen
import dev.alimmz.atlasfly.feature.profile.presentation.ProfileScreen
import dev.alimmz.atlasfly.feature.profile.presentation.SavedPlacesScreen
import dev.alimmz.atlasfly.feature.profile.presentation.SavedTripsScreen
import dev.alimmz.atlasfly.feature.trips.presentation.TripsScreen

/**
 * Resolves a [Routes.Main] destination to the feature module that owns it.
 */
@Composable
fun MainDestination(
    route: Routes.Main,
    sharedViewModel: MainSharedViewModel,
    onNavigate: (Routes) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    when (route) {
        Routes.Main.TopLevel.Home -> HomeScreen(sharedViewModel = sharedViewModel)

        Routes.Main.TopLevel.Explore -> ExploreScreen(sharedViewModel = sharedViewModel)

        Routes.Main.TopLevel.Trips -> TripsScreen(sharedViewModel = sharedViewModel)

        Routes.Main.TopLevel.Planner -> PlannerScreen(sharedViewModel = sharedViewModel)

        Routes.Main.TopLevel.Profile -> ProfileScreen(
            onOpenSavedPlaces = { onNavigate(Routes.Main.SavedPlaces) },
            onOpenSavedTrips = { onNavigate(Routes.Main.SavedTrips) },
            onOpenPreferences = { onNavigate(Routes.Main.Preferences) },
            onOpenAccountSettings = { onNavigate(Routes.Main.AccountSettings) },
            onLogout = onLogout,
        )

        Routes.Main.SavedPlaces -> SavedPlacesScreen(onBack = onBack)

        Routes.Main.SavedTrips -> SavedTripsScreen(onBack = onBack)

        Routes.Main.Preferences -> PreferencesScreen(onBack = onBack)

        Routes.Main.AccountSettings -> AccountSettingsScreen(
            onBack = onBack,
            languageSwitcher = { LanguageSwitcher(expanded = true) },
        )
    }
}
