package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import profile.model.ApiError
import profile.model.ApiResult
import profile.model.DistanceUnit
import profile.model.PatchField
import profile.model.ProfilePreferences
import profile.model.TemperatureUnit
import profile.model.UpdatePreferencesCommand
import profile.usecase.GetPreferencesUseCase
import profile.usecase.UpdatePreferencesUseCase
import javax.inject.Inject

data class PreferencesForm(
    val languageCode: String = "",
    val distanceUnit: DistanceUnit = DistanceUnit.Kilometers,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.Celsius,
    val currencyCode: String = "",
    val timeZone: String = "",
    val flightNotifications: Boolean = false,
    val tripReminders: Boolean = false,
    val marketingNotifications: Boolean = false,
)

data class PreferencesUiState(
    val original: ProfilePreferences? = null,
    val form: PreferencesForm = PreferencesForm(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val preferencesAbsent: Boolean = false,
    val error: ApiError? = null,
    val notice: ProfileNotice? = null,
) {
    val hasChanges: Boolean
        get() = original?.let { form != it.toForm() } ?: preferencesAbsent
}

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val getPreferences: GetPreferencesUseCase,
    private val updatePreferences: UpdatePreferencesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            getPreferences().collect { result ->
                when (result) {
                    ApiResult.Loading -> _uiState.update {
                        it.copy(isLoading = true, error = null, notice = null)
                    }
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            original = result.value,
                            preferencesAbsent = false,
                            form = result.value.toForm(),
                            isLoading = false,
                        )
                    }
                    is ApiResult.Failure -> _uiState.update {
                        if (result.error == ApiError.NotFound) {
                            it.copy(
                                original = null,
                                preferencesAbsent = true,
                                isLoading = false,
                                error = null,
                            )
                        } else {
                            it.copy(isLoading = false, error = result.error)
                        }
                    }
                }
            }
        }
    }

    fun updateForm(transform: (PreferencesForm) -> PreferencesForm) {
        _uiState.update { it.copy(form = transform(it.form), error = null, notice = null) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.hasChanges || state.isSaving) return
        val form = state.form
        val command = UpdatePreferencesCommand(
            languageCode = changed(state.original?.languageCode, form.languageCode.trim()),
            distanceUnit = changed(state.original?.distanceUnit, form.distanceUnit),
            temperatureUnit = changed(state.original?.temperatureUnit, form.temperatureUnit),
            currencyCode = changed(
                state.original?.currencyCode,
                form.currencyCode.trim().uppercase(),
            ),
            timeZone = changed(state.original?.timeZone, form.timeZone.trim()),
            flightNotifications = changed(
                state.original?.flightNotifications,
                form.flightNotifications,
            ),
            tripReminders = changed(state.original?.tripReminders, form.tripReminders),
            marketingNotifications = changed(
                state.original?.marketingNotifications,
                form.marketingNotifications,
            ),
        )
        viewModelScope.launch {
            updatePreferences(command).collect { result ->
                when (result) {
                    ApiResult.Loading -> _uiState.update {
                        it.copy(isSaving = true, error = null, notice = null)
                    }
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            original = result.value,
                            preferencesAbsent = false,
                            form = result.value.toForm(),
                            isSaving = false,
                            notice = ProfileNotice.PreferencesSaved,
                        )
                    }
                    is ApiResult.Failure -> _uiState.update {
                        it.copy(isSaving = false, error = result.error)
                    }
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(error = null, notice = null) }
    }

    private fun <T> changed(old: T?, new: T): PatchField<T> =
        if (!_uiState.value.preferencesAbsent && old == new) {
            PatchField.Omitted
        } else {
            PatchField.Supplied(new)
        }
}

private fun ProfilePreferences.toForm() = PreferencesForm(
    languageCode = languageCode,
    distanceUnit = distanceUnit,
    temperatureUnit = temperatureUnit,
    currencyCode = currencyCode,
    timeZone = timeZone,
    flightNotifications = flightNotifications,
    tripReminders = tripReminders,
    marketingNotifications = marketingNotifications,
)
