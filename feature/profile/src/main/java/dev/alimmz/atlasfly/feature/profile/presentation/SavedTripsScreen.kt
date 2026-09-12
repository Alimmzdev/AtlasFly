package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import profile.model.CreateSavedTripCommand
import profile.model.PatchField
import profile.model.SavedTrip
import profile.model.TripStatus
import profile.model.UpdateSavedTripCommand

@Composable
fun SavedTripsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedTripsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var editedTrip by remember { mutableStateOf<SavedTrip?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<SavedTrip?>(null) }

    ProfileDetailScaffold(
        title = stringResource(R.string.profile_saved_trips),
        onBack = onBack,
        modifier = modifier,
        actions = {
            IconButton(onClick = {
                editedTrip = null
                showEditor = true
            }) {
                Icon(Icons.Outlined.Add, stringResource(R.string.profile_add_trip))
            }
            IconButton(onClick = viewModel::refresh, enabled = !state.isLoading) {
                Icon(Icons.Outlined.Refresh, stringResource(R.string.profile_refresh))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TripFilters(
                status = state.statusFilter,
                archived = state.archivedFilter,
                onStatusChanged = viewModel::setStatusFilter,
                onArchivedChanged = viewModel::setArchivedFilter,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            ProfileFeedback(
                error = state.error,
                notice = state.notice,
                onDismiss = viewModel::clearFeedback,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            when {
                state.isLoading && state.items.isEmpty() -> LoadingContent(Modifier.fillMaxSize())
                state.error != null && state.items.isEmpty() -> ErrorContent(
                    state.error!!,
                    viewModel::refresh,
                    Modifier.fillMaxSize(),
                )
                state.items.isEmpty() -> EmptyContent(
                    stringResource(R.string.profile_no_saved_trips),
                    viewModel::refresh,
                    Modifier.fillMaxSize(),
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.items, key = { it.id }) { trip ->
                        SavedTripCard(
                            trip = trip,
                            enabled = !state.isMutating,
                            onEdit = {
                                editedTrip = trip
                                showEditor = true
                            },
                            onDelete = { pendingDelete = trip },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    if (state.canLoadMore) {
                        item {
                            OutlinedButton(
                                onClick = viewModel::loadMore,
                                enabled = !state.isLoadingMore,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                if (state.isLoadingMore) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.padding(end = 8.dp),
                                    )
                                }
                                Text(stringResource(R.string.profile_load_more))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        SavedTripEditor(
            trip = editedTrip,
            busy = state.isMutating,
            onDismiss = { showEditor = false },
            onCreate = {
                viewModel.create(it)
                showEditor = false
            },
            onUpdate = {
                editedTrip?.let { trip -> viewModel.update(trip.id, it) }
                showEditor = false
            },
        )
    }
    pendingDelete?.let { trip ->
        DeleteDialog(
            itemName = trip.title,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                viewModel.delete(trip.id)
                pendingDelete = null
            },
        )
    }
}

@Composable
private fun TripFilters(
    status: TripStatus?,
    archived: Boolean?,
    onStatusChanged: (TripStatus?) -> Unit,
    onArchivedChanged: (Boolean?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var statusExpanded by remember { mutableStateOf(false) }
    var archivedExpanded by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            OutlinedButton(onClick = { statusExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(status?.label() ?: stringResource(R.string.profile_all_statuses))
            }
            DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.profile_all_statuses)) },
                    onClick = {
                        statusExpanded = false
                        onStatusChanged(null)
                    },
                )
                TripStatus.entries.forEach { value ->
                    DropdownMenuItem(
                        text = { Text(value.label()) },
                        onClick = {
                            statusExpanded = false
                            onStatusChanged(value)
                        },
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            OutlinedButton(onClick = { archivedExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    when (archived) {
                        null -> stringResource(R.string.profile_all_trips)
                        true -> stringResource(R.string.profile_archived)
                        false -> stringResource(R.string.profile_not_archived)
                    },
                )
            }
            DropdownMenu(
                expanded = archivedExpanded,
                onDismissRequest = { archivedExpanded = false },
            ) {
                listOf<Boolean?>(null, false, true).forEach { value ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (value) {
                                    null -> stringResource(R.string.profile_all_trips)
                                    true -> stringResource(R.string.profile_archived)
                                    false -> stringResource(R.string.profile_not_archived)
                                },
                            )
                        },
                        onClick = {
                            archivedExpanded = false
                            onArchivedChanged(value)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedTripCard(
    trip: SavedTrip,
    enabled: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Icon(
                Icons.Outlined.Luggage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(trip.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    trip.destinationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val dates = listOfNotNull(trip.startDate, trip.endDate).joinToString(" – ")
                Text(
                    listOf(dates.takeIf { it.isNotEmpty() }, trip.status.label())
                        .filterNotNull()
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (trip.isArchived) {
                Icon(Icons.Outlined.Archive, stringResource(R.string.profile_archived))
            }
            IconButton(onClick = onEdit, enabled = enabled) {
                Icon(Icons.Outlined.Edit, stringResource(R.string.profile_edit))
            }
            IconButton(onClick = onDelete, enabled = enabled) {
                Icon(
                    Icons.Outlined.Delete,
                    stringResource(R.string.profile_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SavedTripEditor(
    trip: SavedTrip?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onCreate: (CreateSavedTripCommand) -> Unit,
    onUpdate: (UpdateSavedTripCommand) -> Unit,
) {
    var title by rememberSaveable(trip?.id) { mutableStateOf(trip?.title.orEmpty()) }
    var destination by rememberSaveable(trip?.id) { mutableStateOf(trip?.destinationName.orEmpty()) }
    var startDate by rememberSaveable(trip?.id) { mutableStateOf(trip?.startDate.orEmpty()) }
    var endDate by rememberSaveable(trip?.id) { mutableStateOf(trip?.endDate.orEmpty()) }
    var status by remember(trip?.id) { mutableStateOf(trip?.status ?: TripStatus.Draft) }
    var archived by rememberSaveable(trip?.id) { mutableStateOf(trip?.isArchived ?: false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val normalizedTitle = title.trim()
    val normalizedDestination = destination.trim()
    val normalizedStart = startDate.trim().ifEmpty { null }
    val normalizedEnd = endDate.trim().ifEmpty { null }
    val changed = trip == null ||
        normalizedTitle != trip.title ||
        normalizedDestination != trip.destinationName ||
        normalizedStart != trip.startDate ||
        normalizedEnd != trip.endDate ||
        status != trip.status || archived != trip.isArchived

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (trip == null) R.string.profile_add_trip else R.string.profile_edit_trip))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.profile_trip_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text(stringResource(R.string.profile_destination)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text(stringResource(R.string.profile_start_date)) },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text(stringResource(R.string.profile_end_date)) },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Column {
                    OutlinedButton(onClick = { statusExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(status.label())
                    }
                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false },
                    ) {
                        TripStatus.entries.forEach { value ->
                            DropdownMenuItem(
                                text = { Text(value.label()) },
                                onClick = {
                                    status = value
                                    statusExpanded = false
                                },
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = archived, onCheckedChange = { archived = it })
                    Text(stringResource(R.string.profile_archived))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy && normalizedTitle.isNotEmpty() &&
                    normalizedDestination.isNotEmpty() && changed,
                onClick = {
                    if (trip == null) {
                        onCreate(
                            CreateSavedTripCommand(
                                title = normalizedTitle,
                                destinationName = normalizedDestination,
                                startDate = normalizedStart,
                                endDate = normalizedEnd,
                                status = status,
                                archived = archived,
                            ),
                        )
                    } else {
                        onUpdate(
                            UpdateSavedTripCommand(
                                title = changedPatch(trip.title, normalizedTitle),
                                destinationName = changedPatch(
                                    trip.destinationName,
                                    normalizedDestination,
                                ),
                                startDate = changedPatch(trip.startDate, normalizedStart),
                                endDate = changedPatch(trip.endDate, normalizedEnd),
                                status = changedPatch(trip.status, status),
                                archived = changedPatch(trip.isArchived, archived),
                            ),
                        )
                    }
                },
            ) { Text(stringResource(R.string.profile_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_cancel)) }
        },
    )
}

@Composable
private fun TripStatus.label(): String = stringResource(
    when (this) {
        TripStatus.Draft -> R.string.profile_status_draft
        TripStatus.Upcoming -> R.string.profile_status_upcoming
        TripStatus.Active -> R.string.profile_status_active
        TripStatus.Completed -> R.string.profile_status_completed
        TripStatus.Cancelled -> R.string.profile_status_cancelled
    },
)

private fun <T> changedPatch(original: T, current: T): PatchField<T> =
    if (original == current) PatchField.Omitted else PatchField.Supplied(current)
