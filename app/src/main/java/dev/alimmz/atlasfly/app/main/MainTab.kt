package dev.alimmz.atlasfly.app.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import dev.alimmz.atlasfly.R
import dev.alimmz.atlasfly.core.navigation.Routes

/**
 * Bottom navigation bar presentation for each top-level destination.
 */
enum class MainTab(
    val route: Routes.Main.TopLevel,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(
        route = Routes.Main.TopLevel.Home,
        labelRes = R.string.main_tab_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    Explore(
        route = Routes.Main.TopLevel.Explore,
        labelRes = R.string.main_tab_explore,
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
    ),
    Trips(
        route = Routes.Main.TopLevel.Trips,
        labelRes = R.string.main_tab_trips,
        selectedIcon = Icons.Filled.Luggage,
        unselectedIcon = Icons.Outlined.Luggage,
    ),
    Planner(
        route = Routes.Main.TopLevel.Planner,
        labelRes = R.string.main_tab_planner,
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline,
    ),
    Profile(
        route = Routes.Main.TopLevel.Profile,
        labelRes = R.string.main_tab_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
    ),
    ;

    companion object {

        fun fromRoute(route: Routes?): MainTab? = entries.firstOrNull { it.route == route }
    }
}
