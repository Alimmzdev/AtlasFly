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
import profile.model.PatchField
import profile.model.Profile
import profile.model.ProfileImageReference
import profile.model.Subscription
import profile.model.UpdateProfileCommand
import profile.usecase.GetProfileUseCase
import profile.usecase.GetSubscriptionUseCase
import profile.usecase.UpdateProfileUseCase
import profile.usecase.UploadProfileImageUseCase
import javax.inject.Inject

data class ProfileUiState(
    val profile: Profile? = null,
    val subscription: Subscription? = null,
    val isLoadingProfile: Boolean = false,
    val isLoadingSubscription: Boolean = false,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val profileAbsent: Boolean = false,
    val error: ApiError? = null,
    val notice: ProfileNotice? = null,
) {
    val isLoading: Boolean get() = isLoadingProfile || isLoadingSubscription
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val getSubscription: GetSubscriptionUseCase,
    private val uploadProfileImage: UploadProfileImageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var profileJob: Job? = null
    private var subscriptionJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        loadProfile()
        loadSubscription()
    }

    fun saveDisplayName(value: String?) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            updateProfile(
                UpdateProfileCommand(displayName = PatchField.Supplied(value?.trim()?.ifEmpty { null })),
            ).collect { result ->
                when (result) {
                    ApiResult.Loading -> _uiState.update {
                        it.copy(isSaving = true, error = null, notice = null)
                    }
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            profile = result.value,
                            profileAbsent = false,
                            isSaving = false,
                            notice = ProfileNotice.ProfileSaved,
                        )
                    }
                    is ApiResult.Failure -> _uiState.update {
                        it.copy(isSaving = false, error = result.error)
                    }
                }
            }
        }
    }

    fun uploadImage(contentUri: String) {
        if (_uiState.value.isUploading) return
        viewModelScope.launch {
            uploadProfileImage(ProfileImageReference(contentUri)).collect { result ->
                when (result) {
                    ApiResult.Loading -> _uiState.update {
                        it.copy(isUploading = true, error = null, notice = null)
                    }
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(isUploading = false, notice = ProfileNotice.ImageUploaded)
                        }
                        loadProfile(preserveNotice = true)
                    }
                    is ApiResult.Failure -> _uiState.update {
                        it.copy(isUploading = false, error = result.error)
                    }
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(error = null, notice = null) }
    }

    private fun loadProfile(preserveNotice: Boolean = false) {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            getProfile().collect { result ->
                when (result) {
                    ApiResult.Loading -> _uiState.update {
                        it.copy(
                            isLoadingProfile = true,
                            error = null,
                            notice = it.notice.takeIf { preserveNotice },
                        )
                    }
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            profile = result.value,
                            profileAbsent = false,
                            isLoadingProfile = false,
                        )
                    }
                    is ApiResult.Failure -> _uiState.update {
                        if (result.error == ApiError.NotFound) {
                            it.copy(
                                profile = null,
                                profileAbsent = true,
                                isLoadingProfile = false,
                                error = null,
                            )
                        } else {
                            it.copy(isLoadingProfile = false, error = result.error)
                        }
                    }
                }
            }
        }
    }

    private fun loadSubscription() {
        subscriptionJob?.cancel()
        subscriptionJob = viewModelScope.launch {
            getSubscription().collect { result ->
                when (result) {
                    ApiResult.Loading -> _uiState.update {
                        it.copy(isLoadingSubscription = true)
                    }
                    is ApiResult.Success -> _uiState.update {
                        it.copy(subscription = result.value, isLoadingSubscription = false)
                    }
                    is ApiResult.Failure -> _uiState.update {
                        it.copy(isLoadingSubscription = false, error = result.error)
                    }
                }
            }
        }
    }
}
