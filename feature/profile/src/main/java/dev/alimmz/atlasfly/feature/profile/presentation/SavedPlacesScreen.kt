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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import profile.model.CreateSavedPlaceCommand
import profile.model.PatchField
import profile.model.SavedPlace
import profile.model.UpdateSavedPlaceCommand

@Composable
fun SavedPlacesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedPlacesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var editedPlace by remember { mutableStateOf<SavedPlace?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<SavedPlace?>(null) }

    ProfileDetailScaffold(
        title = stringResource(R.string.profile_saved_places),
        onBack = onBack,
        modifier = modifier,
        actions = {
            IconButton(
                onClick = {
                    editedPlace = null
                    showEditor = true
                },
            ) {
                Icon(Icons.Outlined.Add, stringResource(R.string.profile_add_place))
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                OutlinedTextField(
                    value = state.countryCode,
                    onValueChange = viewModel::setCountryCode,
                    label = { Text(stringResource(R.string.profile_country_code_filter)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.FilterAlt, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = viewModel::applyFilter) {
                    Text(stringResource(R.string.profile_apply))
                }
            }
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
                    stringResource(R.string.profile_no_saved_places),
                    viewModel::refresh,
                    Modifier.fillMaxSize(),
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.items, key = { it.id }) { place ->
                        SavedPlaceCard(
                            place = place,
                            enabled = !state.isMutating,
                            onEdit = {
                                editedPlace = place
                                showEditor = true
                            },
                            onDelete = { pendingDelete = place },
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
        SavedPlaceEditor(
            place = editedPlace,
            busy = state.isMutating,
            onDismiss = { showEditor = false },
            onSave = { create, update ->
                editedPlace?.let { viewModel.update(it.id, update) }
                    ?: viewModel.create(create)
                showEditor = false
            },
        )
    }
    pendingDelete?.let { place ->
        DeleteDialog(
            itemName = place.name,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                viewModel.delete(place.id)
                pendingDelete = null
            },
        )
    }
}

@Composable
private fun SavedPlaceCard(
    place: SavedPlace,
    enabled: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                Icons.Outlined.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, style = MaterialTheme.typography.titleMedium)
                place.address?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val detail = listOfNotNull(place.countryCode, place.notes).joinToString(" • ")
                if (detail.isNotEmpty()) {
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
private fun SavedPlaceEditor(
    place: SavedPlace?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (CreateSavedPlaceCommand, UpdateSavedPlaceCommand) -> Unit,
) {
    var name by rememberSaveable(place?.id) { mutableStateOf(place?.name.orEmpty()) }
    var address by rememberSaveable(place?.id) { mutableStateOf(place?.address.orEmpty()) }
    var countryCode by rememberSaveable(place?.id) { mutableStateOf(place?.countryCode.orEmpty()) }
    var notes by rememberSaveable(place?.id) { mutableStateOf(place?.notes.orEmpty()) }
    val normalizedName = name.trim()
    val normalizedAddress = address.trim().ifEmpty { null }
    val normalizedCountry = countryCode.trim().uppercase().ifEmpty { null }
    val normalizedNotes = notes.trim().ifEmpty { null }
    val changed = place == null ||
        normalizedName != place.name ||
        normalizedAddress != place.address ||
        normalizedCountry != place.countryCode ||
        normalizedNotes != place.notes

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (place == null) R.string.profile_add_place else R.string.profile_edit_place))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.profile_place_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(stringResource(R.string.profile_place_address)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = countryCode,
                    onValueChange = { countryCode = it.take(2).uppercase() },
                    label = { Text(stringResource(R.string.profile_country_code)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.profile_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy && normalizedName.isNotEmpty() && changed,
                onClick = {
                    onSave(
                        CreateSavedPlaceCommand(
                            name = normalizedName,
                            address = normalizedAddress,
                            countryCode = normalizedCountry,
                            notes = normalizedNotes,
                        ),
                        UpdateSavedPlaceCommand(
                            name = if (place != null && place.name != normalizedName) {
                                PatchField.Supplied(normalizedName)
                            } else {
                                PatchField.Omitted
                            },
                            address = patch(place?.address, normalizedAddress, place != null),
                            countryCode = patch(place?.countryCode, normalizedCountry, place != null),
                            notes = patch(place?.notes, normalizedNotes, place != null),
                        ),
                    )
                },
            ) { Text(stringResource(R.string.profile_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_cancel)) }
        },
    )
}

@Composable
internal fun DeleteDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_delete_title)) },
        text = { Text(stringResource(R.string.profile_delete_message, itemName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.profile_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_cancel)) }
        },
    )
}

private fun <T> patch(original: T, current: T, enabled: Boolean): PatchField<T> =
    if (enabled && original != current) PatchField.Supplied(current) else PatchField.Omitted
