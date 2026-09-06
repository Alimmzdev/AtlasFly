package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    onOpenSavedPlaces: () -> Unit,
    onOpenSavedTrips: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenAccountSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
        )

        ProfileMenuItem(
            titleRes = R.string.profile_saved_places,
            icon = Icons.Outlined.Place,
            onClick = onOpenSavedPlaces,
        )
        ProfileMenuItem(
            titleRes = R.string.profile_saved_trips,
            icon = Icons.Outlined.Luggage,
            onClick = onOpenSavedTrips,
        )
        ProfileMenuItem(
            titleRes = R.string.profile_preferences,
            icon = Icons.Outlined.Tune,
            onClick = onOpenPreferences,
        )
        ProfileMenuItem(
            titleRes = R.string.profile_account_settings,
            icon = Icons.Outlined.ManageAccounts,
            onClick = onOpenAccountSettings,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(R.string.profile_logout),
                    color = MaterialTheme.colorScheme.error,
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.clickable(onClick = onLogout),
        )
    }
}

@Composable
private fun ProfileMenuItem(
    @StringRes titleRes: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(text = stringResource(titleRes)) },
        leadingContent = {
            Icon(imageVector = icon, contentDescription = null)
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.clickable(onClick = onClick),
    )
}
