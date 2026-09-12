package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import profile.model.ApiError
import profile.model.ApiResult
import profile.model.CreateSavedTripCommand
import profile.model.SavedTrip
import profile.model.SavedTripsQuery
import profile.model.TripStatus
import profile.model.UpdateSavedTripCommand
import profile.usecase.CreateSavedTripUseCase
import profile.usecase.DeleteSavedTripUseCase
import profile.usecase.GetSavedTripsUseCase
import profile.usecase.UpdateSavedTripUseCase
import javax.inject.Inject

data class SavedTripsUiState(
    val items: List<SavedTrip> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isMutating: Boolean = false,
    val canLoadMore: Boolean = false,
    val statusFilter: TripStatus? = null,
    val archivedFilter: Boolean? = null,
    val error: ApiError? = null,
    val notice: ProfileNotice? = null,
)

@HiltViewModel
class SavedTripsViewModel @Inject constructor(
    private val getSavedTrips: GetSavedTripsUseCase,
    private val createSavedTrip: CreateSavedTripUseCase,
    private val updateSavedTrip: UpdateSavedTripUseCase,
    private val deleteSavedTrip: DeleteSavedTripUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedTripsUiState())
    val uiState: StateFlow<SavedTripsUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun setStatusFilter(value: TripStatus?) {
        _uiState.update { it.copy(statusFilter = value) }
        refresh()
    }

    fun setArchivedFilter(value: Boolean?) {
        _uiState.update { it.copy(archivedFilter = value) }
        refresh()
    }

    fun refresh() = load(0, append = false)

    fun loadMore() {
        val state = _uiState.value
        if (!state.canLoadMore || state.isLoading || state.isLoadingMore) return
        load(state.items.size, append = true)
    }

    fun create(command: CreateSavedTripCommand) = mutate(
        notice = ProfileNotice.TripCreated,
        operation = { createSavedTrip(command) },
        merge = { items, saved -> listOf(saved) + items },
    )

    fun update(id: String, command: UpdateSavedTripCommand) = mutate(
        notice = ProfileNotice.TripUpdated,
        operation = { updateSavedTrip(id, command) },
        merge = { items, saved -> items.map { if (it.id == saved.id) saved else it } },
    )

    fun delete(id: String) {
        if (_uiState.value.isMutating) return
        viewModelScope.launch {
            deleteSavedTrip(id).collect { result ->
                when (result) {
                    ApiResult.Loading -> startMutation()
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            items = it.items.filterNot { item -> item.id == id },
                            isMutating = false,
                            notice = ProfileNotice.TripDeleted,
                        )
                    }
                    is ApiResult.Failure -> mutationFailed(result.error)
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(error = null, notice = null) }
    }

    private fun load(offset: Int, append: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val state = _uiState.value
            getSavedTrips(
                SavedTripsQuery(
                    limit = PAGE_SIZE,
                    offset = offset,
                    status = state.statusFilter,
                    archived = state.archivedFilter,
                ),
            ).collect { result ->
                when (result) {
                    ApiResult.Loading -> _uiState.update {
                        it.copy(
                            isLoading = !append,
                            isLoadingMore = append,
                            error = null,
                            notice = null,
                        )
                    }
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            items = if (append) it.items + result.value.items else result.value.items,
                            isLoading = false,
                            isLoadingMore = false,
                            canLoadMore = result.value.count >= result.value.limit,
                        )
                    }
                    is ApiResult.Failure -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = result.error,
                        )
                    }
                }
            }
        }
    }

    private fun mutate(
        notice: ProfileNotice,
        operation: () -> Flow<ApiResult<SavedTrip>>,
        merge: (List<SavedTrip>, SavedTrip) -> List<SavedTrip>,
    ) {
        if (_uiState.value.isMutating) return
        viewModelScope.launch {
            operation().collect { result ->
                when (result) {
                    ApiResult.Loading -> startMutation()
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            items = merge(it.items, result.value),
                            isMutating = false,
                            notice = notice,
                        )
                    }
                    is ApiResult.Failure -> mutationFailed(result.error)
                }
            }
        }
    }

    private fun startMutation() {
        _uiState.update { it.copy(isMutating = true, error = null, notice = null) }
    }

    private fun mutationFailed(error: ApiError) {
        _uiState.update { it.copy(isMutating = false, error = error) }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
