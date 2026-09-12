package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import profile.model.SubscriptionPlan
import profile.model.SubscriptionStatus

@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    languageSwitcher: @Composable () -> Unit,
    selectedImageUri: String?,
    onSelectImage: () -> Unit,
    onImageConsumed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var displayName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state.profile?.displayName) {
        displayName = state.profile?.displayName.orEmpty()
    }
    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let {
            viewModel.uploadImage(it)
            onImageConsumed()
        }
    }

    ProfileDetailScaffold(
        title = stringResource(R.string.profile_account_settings),
        onBack = onBack,
        modifier = modifier,
        actions = {
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Outlined.Refresh, stringResource(R.string.profile_refresh))
            }
        },
    ) { padding ->
        when {
            state.isLoading && state.profile == null -> LoadingContent(
                Modifier.fillMaxSize().padding(padding),
            )
            state.error != null && state.profile == null -> ErrorContent(
                state.error!!,
                viewModel::refresh,
                Modifier.fillMaxSize().padding(padding),
            )
            else -> Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                ProfileFeedback(state.error, state.notice, viewModel::clearFeedback)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    ) {
                        Text(
                            text = displayName.trim().firstOrNull()?.uppercase() ?: "A",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = onSelectImage,
                            enabled = !state.isUploading,
                        ) {
                            if (state.isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(Icons.Outlined.AddAPhoto, contentDescription = null)
                            }
                            Text(
                                stringResource(R.string.profile_change_photo),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        Text(
                            stringResource(R.string.profile_photo_requirements),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (state.profile?.avatarPath != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            stringResource(R.string.profile_private_photo_saved),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it.take(100) },
                    label = { Text(stringResource(R.string.profile_display_name)) },
                    supportingText = { Text(stringResource(R.string.profile_display_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        viewModel.saveDisplayName(displayName.trim().ifEmpty { null })
                    },
                    enabled = !state.isSaving &&
                        displayName.trim().ifEmpty { null } != state.profile?.displayName,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(
                            if (state.isSaving) R.string.profile_saving else R.string.profile_save_changes,
                        ),
                    )
                }
                SubscriptionCard(
                    plan = state.subscription?.plan,
                    status = state.subscription?.status,
                    periodEnd = state.subscription?.currentPeriodEnd,
                )
                Text(
                    stringResource(R.string.profile_app_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                languageSwitcher()
                state.profile?.userId?.let { userId ->
                    Text(
                        text = stringResource(R.string.profile_account_id, userId),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    plan: SubscriptionPlan?,
    status: SubscriptionStatus?,
    periodEnd: String?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(stringResource(R.string.profile_subscription), style = MaterialTheme.typography.titleMedium)
            Text(
                when (plan) {
                    SubscriptionPlan.Free -> stringResource(R.string.profile_plan_free)
                    SubscriptionPlan.Premium -> stringResource(R.string.profile_plan_premium)
                    null -> stringResource(R.string.profile_plan_loading)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            status?.let {
                Text(
                    stringResource(R.string.profile_subscription_status, it.label()),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            periodEnd?.let {
                Text(
                    stringResource(R.string.profile_period_end, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SubscriptionStatus.label(): String = stringResource(
    when (this) {
        SubscriptionStatus.Free -> R.string.profile_subscription_free
        SubscriptionStatus.Trialing -> R.string.profile_subscription_trialing
        SubscriptionStatus.Active -> R.string.profile_subscription_active
        SubscriptionStatus.PastDue -> R.string.profile_subscription_past_due
        SubscriptionStatus.Cancelled -> R.string.profile_subscription_cancelled
        SubscriptionStatus.Expired -> R.string.profile_subscription_expired
    },
)
