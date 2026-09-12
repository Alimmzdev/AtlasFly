package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import profile.model.DistanceUnit
import profile.model.TemperatureUnit

@Composable
fun PreferencesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    ProfileDetailScaffold(
        title = stringResource(R.string.profile_preferences),
        onBack = onBack,
        modifier = modifier,
    ) { padding ->
        when {
            state.isLoading && state.original == null -> LoadingContent(
                Modifier.fillMaxSize().padding(padding),
            )
            state.error != null && state.original == null -> ErrorContent(
                state.error!!,
                viewModel::refresh,
                Modifier.fillMaxSize().padding(padding),
            )
            state.original != null || state.preferencesAbsent -> PreferencesFormContent(
                state = state,
                onFormChanged = viewModel::updateForm,
                onSave = viewModel::save,
                onDismissFeedback = viewModel::clearFeedback,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun PreferencesFormContent(
    state: PreferencesUiState,
    onFormChanged: ((PreferencesForm) -> PreferencesForm) -> Unit,
    onSave: () -> Unit,
    onDismissFeedback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        ProfileFeedback(state.error, state.notice, onDismissFeedback)
        Text(
            stringResource(R.string.profile_regional_preferences),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = form.languageCode,
            onValueChange = { value -> onFormChanged { it.copy(languageCode = value) } },
            label = { Text(stringResource(R.string.profile_language_code)) },
            supportingText = { Text(stringResource(R.string.profile_language_code_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.currencyCode,
            onValueChange = { value ->
                onFormChanged { it.copy(currencyCode = value.take(3).uppercase()) }
            },
            label = { Text(stringResource(R.string.profile_currency_code)) },
            supportingText = { Text(stringResource(R.string.profile_currency_code_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.timeZone,
            onValueChange = { value -> onFormChanged { it.copy(timeZone = value) } },
            label = { Text(stringResource(R.string.profile_time_zone)) },
            supportingText = { Text(stringResource(R.string.profile_time_zone_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UnitSelector(
                label = stringResource(R.string.profile_distance_unit),
                value = form.distanceUnit.label(),
                values = DistanceUnit.entries.map { it to it.label() },
                onSelected = { value -> onFormChanged { it.copy(distanceUnit = value) } },
                modifier = Modifier.weight(1f),
            )
            UnitSelector(
                label = stringResource(R.string.profile_temperature_unit),
                value = form.temperatureUnit.label(),
                values = TemperatureUnit.entries.map { it to it.label() },
                onSelected = { value -> onFormChanged { it.copy(temperatureUnit = value) } },
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            stringResource(R.string.profile_notifications),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        PreferenceSwitch(
            title = stringResource(R.string.profile_flight_notifications),
            checked = form.flightNotifications,
            onCheckedChange = { value -> onFormChanged { it.copy(flightNotifications = value) } },
        )
        PreferenceSwitch(
            title = stringResource(R.string.profile_trip_reminders),
            checked = form.tripReminders,
            onCheckedChange = { value -> onFormChanged { it.copy(tripReminders = value) } },
        )
        PreferenceSwitch(
            title = stringResource(R.string.profile_marketing_notifications),
            checked = form.marketingNotifications,
            onCheckedChange = { value -> onFormChanged { it.copy(marketingNotifications = value) } },
        )
        Button(
            onClick = onSave,
            enabled = state.hasChanges && !state.isSaving &&
                form.languageCode.isNotBlank() &&
                form.currencyCode.length == 3 &&
                form.timeZone.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp),
        ) {
            Text(
                stringResource(
                    if (state.isSaving) R.string.profile_saving else R.string.profile_save_changes,
                ),
            )
        }
    }
}

@Composable
private fun <T> UnitSelector(
    label: String,
    value: String,
    values: List<Pair<T, String>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(value)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { (item, itemLabel) ->
                DropdownMenuItem(
                    text = { Text(itemLabel) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PreferenceSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DistanceUnit.label(): String = when (this) {
    DistanceUnit.Kilometers -> stringResource(R.string.profile_kilometers)
    DistanceUnit.Miles -> stringResource(R.string.profile_miles)
}

@Composable
private fun TemperatureUnit.label(): String = when (this) {
    TemperatureUnit.Celsius -> stringResource(R.string.profile_celsius)
    TemperatureUnit.Fahrenheit -> stringResource(R.string.profile_fahrenheit)
}
