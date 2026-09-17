package profile.repository

import kotlinx.coroutines.flow.Flow
import profile.model.ApiResult
import profile.model.CreateSavedPlaceCommand
import profile.model.CreateSavedTripCommand
import profile.model.Page
import profile.model.Profile
import profile.model.ProfileImagePath
import profile.model.ProfileImageReference
import profile.model.ProfilePreferences
import profile.model.PushTokenRegistration
import profile.model.RegisterPushTokenCommand
import profile.model.SavedPlace
import profile.model.SavedPlacesQuery
import profile.model.SavedTrip
import profile.model.SavedTripsQuery
import profile.model.Subscription
import profile.model.UpdatePreferencesCommand
import profile.model.UpdateProfileCommand
import profile.model.UpdatePushTokenCommand
import profile.model.UpdateSavedPlaceCommand
import profile.model.UpdateSavedTripCommand

interface ProfileRepository {
    fun getProfile(): Flow<ApiResult<Profile>>
    fun updateProfile(command: UpdateProfileCommand): Flow<ApiResult<Profile>>
    fun getPreferences(): Flow<ApiResult<ProfilePreferences>>
    fun updatePreferences(command: UpdatePreferencesCommand): Flow<ApiResult<ProfilePreferences>>
    fun getSavedPlaces(query: SavedPlacesQuery): Flow<ApiResult<Page<SavedPlace>>>
    fun createSavedPlace(command: CreateSavedPlaceCommand): Flow<ApiResult<SavedPlace>>
    fun updateSavedPlace(id: String, command: UpdateSavedPlaceCommand): Flow<ApiResult<SavedPlace>>
    fun deleteSavedPlace(id: String): Flow<ApiResult<Unit>>
    fun getSavedTrips(query: SavedTripsQuery): Flow<ApiResult<Page<SavedTrip>>>
    fun createSavedTrip(command: CreateSavedTripCommand): Flow<ApiResult<SavedTrip>>
    fun updateSavedTrip(id: String, command: UpdateSavedTripCommand): Flow<ApiResult<SavedTrip>>
    fun deleteSavedTrip(id: String): Flow<ApiResult<Unit>>
    fun registerPushToken(command: RegisterPushTokenCommand): Flow<ApiResult<PushTokenRegistration>>
    fun updatePushToken(id: String, command: UpdatePushTokenCommand): Flow<ApiResult<PushTokenRegistration>>
    fun deletePushToken(id: String): Flow<ApiResult<Unit>>
    fun getSubscription(): Flow<ApiResult<Subscription>>
    fun uploadProfileImage(reference: ProfileImageReference): Flow<ApiResult<ProfileImagePath>>
}
