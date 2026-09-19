package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import coil3.compose.AsyncImage
import profile.model.SubscriptionPlan
import profile.model.SubscriptionStatus
import androidx.core.net.toUri

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
    val colors = if (isSystemInDarkTheme()) MaterialTheme.colorScheme else lightColorScheme(
        primary = Color(0xFF008E9D), onPrimary = Color.White,
        primaryContainer = Color(0xFFE4F5F7), onPrimaryContainer = Color(0xFF007B87),
        background = Color(0xFFF4F7FA), surface = Color.White,
        surfaceContainerLow = Color.White, surfaceContainer = Color(0xFFF2F6F9),
        onSurface = Color(0xFF0D1933), onBackground = Color(0xFF0D1933),
        onSurfaceVariant = Color(0xFF65758D), outline = Color(0xFFBFCEDC),
        outlineVariant = Color(0xFFD8E2EB),
    )
    MaterialTheme(colorScheme = colors) {
        AccountSettingsContent(onBack, languageSwitcher, selectedImageUri, onSelectImage,
            onImageConsumed, modifier, viewModel)
    }
}

@Composable
private fun AccountSettingsContent(
    onBack: () -> Unit,
    languageSwitcher: @Composable () -> Unit,
    selectedImageUri: String?,
    onSelectImage: () -> Unit,
    onImageConsumed: () -> Unit,
    modifier: Modifier,
    viewModel: ProfileViewModel,
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                ProfileFeedback(state.error, state.notice, viewModel::clearFeedback)
                ProfileIdentityHeader(
                    displayName = displayName,
                    userId = state.profile?.userId,
                    avatarUrl = state.profile?.avatarUrl,
                    avatarRefreshVersion = state.avatarRefreshVersion,
                    isUploading = state.isUploading,
                    onChangePhoto = onSelectImage,
                )
                AccountGroup(
                    title = stringResource(R.string.profile_personal_details),
                    subtitle = stringResource(R.string.account_details_subtitle),
                    icon = Icons.Outlined.PersonOutline,
                ) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it.take(100) },
                        label = { Text(stringResource(R.string.profile_display_name)) },
                        supportingText = { Text(stringResource(R.string.profile_display_name_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    )
                    Button(
                        onClick = {
                            viewModel.saveDisplayName(displayName.trim().ifEmpty { null })
                        },
                        enabled = !state.isSaving &&
                            displayName.trim().ifEmpty { null } != state.profile?.displayName,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(
                            stringResource(
                                if (state.isSaving) {
                                    R.string.profile_saving
                                } else {
                                    R.string.profile_save_changes
                                },
                            ),
                        )
                    }
                }
                SubscriptionCard(
                    plan = state.subscription?.plan,
                    status = state.subscription?.status,
                    periodEnd = state.subscription?.currentPeriodEnd,
                )
                AccountGroup(
                    title = stringResource(R.string.profile_app_settings),
                    subtitle = stringResource(R.string.account_language_subtitle),
                    icon = Icons.Outlined.Language,
                ) {
                    languageSwitcher()
                }
                state.profile?.userId?.let { userId ->
                    Text(
                        text = stringResource(R.string.profile_account_id, userId),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileIdentityHeader(
    displayName: String,
    userId: String?,
    avatarUrl: String?,
    avatarRefreshVersion: Long,
    isUploading: Boolean,
    onChangePhoto: () -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
    MountainBackdrop(Modifier.fillMaxWidth().height(150.dp))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
    ) {
        Box {
        Surface(shape = CircleShape, border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
            shadowElevation = 6.dp) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(112.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        ) {
            Text(
                text = displayName.trim().firstOrNull()?.uppercase() ?: "A",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
            avatarUrl
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?.let { url ->
                    AsyncImage(
                        model = url.withAvatarVersion(avatarRefreshVersion),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize().clip(CircleShape),
                    )
                }
        }
        }
        Surface(
            onClick = onChangePhoto,
            enabled = !isUploading,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.surface),
            shadowElevation = 3.dp,
            modifier = Modifier.align(Alignment.BottomEnd).size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isUploading) CircularProgressIndicator(Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Icon(Icons.Outlined.AddAPhoto,
                    contentDescription = stringResource(R.string.profile_change_photo),
                    modifier = Modifier.size(22.dp))
            }
        }
        }
        Text(
            text = displayName.ifBlank { stringResource(R.string.profile_unnamed_user) },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        userId?.let {
            Text(
                text = stringResource(R.string.profile_account_id, it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    }
}

@Composable
private fun AccountGroup(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                content()
            }
        }
}

private fun String.withAvatarVersion(version: Long): String =
    if (version == 0L) this else this.toUri()
        .buildUpon()
        .appendQueryParameter("v", version.toString())
        .build()
        .toString()

@Composable
private fun SubscriptionCard(
    plan: SubscriptionPlan?,
    status: SubscriptionStatus?,
    periodEnd: String?,
) {
    AccountGroup(title = stringResource(R.string.profile_subscription),
        subtitle = stringResource(R.string.account_subscription_subtitle),
        icon = Icons.Outlined.WorkspacePremium) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            Text(
                text = when (plan) {
                    SubscriptionPlan.Free -> stringResource(R.string.profile_plan_free)
                    SubscriptionPlan.Premium -> stringResource(R.string.profile_plan_premium)
                    null -> stringResource(R.string.profile_plan_loading)
                },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            status?.let {
                Text(
                    text = it.label(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
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

@Composable
private fun MountainBackdrop(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Box(modifier) {
        Canvas(Modifier.matchParentSize()) {
            val ridge = Path().apply {
                moveTo(0f, size.height * .78f)
                lineTo(size.width * .06f, size.height * .57f)
                lineTo(size.width * .17f, size.height * .77f)
                lineTo(size.width * .29f, size.height * .56f)
                lineTo(size.width * .46f, size.height * .85f)
                lineTo(size.width * .66f, size.height * .49f)
                lineTo(size.width * .74f, size.height * .65f)
                lineTo(size.width * .91f, size.height * .27f)
                lineTo(size.width, size.height * .48f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(ridge, Brush.verticalGradient(listOf(color.copy(alpha = .14f), Color.Transparent)))
            val route = Path().apply {
                moveTo(size.width * .62f, size.height * .5f)
                quadraticTo(size.width * .7f, size.height * .2f,
                    size.width * .84f, size.height * .2f)
            }
            drawPath(route, color.copy(alpha = .25f), style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(6.dp.toPx(), 5.dp.toPx()))))
        }
        Icon(Icons.Outlined.Flight, null, tint = color.copy(alpha = .25f),
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 40.dp).size(25.dp))
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
