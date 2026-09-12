package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import profile.model.SubscriptionPlan

@Composable
fun ProfileScreen(
    onOpenSavedPlaces: () -> Unit,
    onOpenSavedTrips: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenAccountSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = viewModel::refresh, enabled = !uiState.isLoading) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Refresh, stringResource(R.string.profile_refresh))
                }
            }
        }

        ProfileFeedback(
            error = uiState.error,
            notice = uiState.notice,
            onDismiss = viewModel::clearFeedback,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        ProfileHeader(
            displayName = uiState.profile?.displayName,
            userId = uiState.profile?.userId,
            plan = uiState.subscription?.plan,
            onClick = onOpenAccountSettings,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        ProfileMenuItem(
            titleRes = R.string.profile_saved_places,
            subtitleRes = R.string.profile_saved_places_subtitle,
            icon = Icons.Outlined.Place,
            onClick = onOpenSavedPlaces,
        )
        ProfileMenuItem(
            titleRes = R.string.profile_saved_trips,
            subtitleRes = R.string.profile_saved_trips_subtitle,
            icon = Icons.Outlined.Luggage,
            onClick = onOpenSavedTrips,
        )
        ProfileMenuItem(
            titleRes = R.string.profile_preferences,
            subtitleRes = R.string.profile_preferences_subtitle,
            icon = Icons.Outlined.Tune,
            onClick = onOpenPreferences,
        )
        ProfileMenuItem(
            titleRes = R.string.profile_account_settings,
            subtitleRes = R.string.profile_account_settings_subtitle,
            icon = Icons.Outlined.ManageAccounts,
            onClick = onOpenAccountSettings,
        )

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
        ListItem(
            headlineContent = {
                Text(stringResource(R.string.profile_logout), color = MaterialTheme.colorScheme.error)
            },
            leadingContent = {
                Icon(
                    Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.clickable(onClick = onLogout),
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileHeader(
    displayName: String?,
    userId: String?,
    plan: SubscriptionPlan?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            ) {
                Text(
                    text = displayName?.trim()?.firstOrNull()?.uppercase() ?: "A",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName ?: stringResource(R.string.profile_unnamed_user),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = userId ?: stringResource(R.string.profile_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = when (plan) {
                        SubscriptionPlan.Premium -> stringResource(R.string.profile_plan_premium)
                        SubscriptionPlan.Free -> stringResource(R.string.profile_plan_free)
                        null -> stringResource(R.string.profile_plan_loading)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(titleRes)) },
        supportingContent = { Text(stringResource(subtitleRes)) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable(onClick = onClick),
    )
}
