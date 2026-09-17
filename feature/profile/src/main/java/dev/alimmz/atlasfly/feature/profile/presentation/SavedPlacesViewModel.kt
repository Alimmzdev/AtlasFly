package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import profile.model.ApiError
import profile.model.ApiResult
import profile.model.CreateSavedPlaceCommand
import profile.model.SavedPlace
import profile.model.SavedPlacesQuery
import profile.model.UpdateSavedPlaceCommand
import profile.usecase.CreateSavedPlaceUseCase
import profile.usecase.DeleteSavedPlaceUseCase
import profile.usecase.GetSavedPlacesUseCase
import profile.usecase.UpdateSavedPlaceUseCase
import javax.inject.Inject

data class SavedPlacesUiState(
    val items: List<SavedPlace> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isMutating: Boolean = false,
    val canLoadMore: Boolean = false,
    val countryCode: String = "",
    val error: ApiError? = null,
    val notice: ProfileNotice? = null,
)

@HiltViewModel
class SavedPlacesViewModel @Inject constructor(
    private val getSavedPlaces: GetSavedPlacesUseCase,
    private val createSavedPlace: CreateSavedPlaceUseCase,
    private val updateSavedPlace: UpdateSavedPlaceUseCase,
    private val deleteSavedPlace: DeleteSavedPlaceUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedPlacesUiState())
    val uiState: StateFlow<SavedPlacesUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun setCountryCode(value: String) {
        _uiState.update { it.copy(countryCode = value.take(2).uppercase()) }
    }

    fun applyFilter() = refresh()

    fun refresh() = load(offset = 0, append = false)

    fun loadMore() {
        val state = _uiState.value
        if (!state.canLoadMore || state.isLoading || state.isLoadingMore) return
        load(offset = state.items.size, append = true)
    }

    fun create(command: CreateSavedPlaceCommand) = mutate(
        notice = ProfileNotice.PlaceCreated,
        operation = { createSavedPlace(command) },
        merge = { items, saved -> listOf(saved) + items },
    )

    fun update(id: String, command: UpdateSavedPlaceCommand) = mutate(
        notice = ProfileNotice.PlaceUpdated,
        operation = { updateSavedPlace(id, command) },
        merge = { items, saved -> items.map { if (it.id == saved.id) saved else it } },
    )

    fun delete(id: String) {
        if (_uiState.value.isMutating) return
        viewModelScope.launch {
            deleteSavedPlace(id).collect { result ->
                when (result) {
                    ApiResult.Loading -> startMutation()
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            items = it.items.filterNot { item -> item.id == id },
                            isMutating = false,
                            notice = ProfileNotice.PlaceDeleted,
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
            val filter = _uiState.value.countryCode.trim().ifEmpty { null }
            getSavedPlaces(SavedPlacesQuery(PAGE_SIZE, offset, filter)).collect { result ->
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
        operation: () -> kotlinx.coroutines.flow.Flow<ApiResult<SavedPlace>>,
        merge: (List<SavedPlace>, SavedPlace) -> List<SavedPlace>,
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
