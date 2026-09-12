package profile.datasource.remote

import profile.model.CreateSavedPlaceRequestDto
import profile.model.CreateSavedTripRequestDto
import profile.model.PatchRequestDto
import profile.model.PreparedProfileImage
import profile.model.ProfileDto
import profile.model.ProfilePreferencesDto
import profile.model.PushTokenRegistrationDto
import profile.model.RegisterPushTokenRequestDto
import profile.model.SavedPlaceDto
import profile.model.SavedPlacesPageDto
import profile.model.SavedTripDto
import profile.model.SavedTripsPageDto
import profile.model.SubscriptionDto
import profile.model.UploadProfileImageResponseDto

interface ProfileRemoteDataSource {
    suspend fun getProfile(): ProfileDto
    suspend fun updateProfile(request: PatchRequestDto): ProfileDto
    suspend fun getPreferences(): ProfilePreferencesDto
    suspend fun updatePreferences(request: PatchRequestDto): ProfilePreferencesDto
    suspend fun getSavedPlaces(limit: Int?, offset: Int?, countryCode: String?): SavedPlacesPageDto
    suspend fun createSavedPlace(request: CreateSavedPlaceRequestDto): SavedPlaceDto
    suspend fun updateSavedPlace(id: String, request: PatchRequestDto): SavedPlaceDto
    suspend fun deleteSavedPlace(id: String)
    suspend fun getSavedTrips(limit: Int?, offset: Int?, status: String?, archived: Boolean?): SavedTripsPageDto
    suspend fun createSavedTrip(request: CreateSavedTripRequestDto): SavedTripDto
    suspend fun updateSavedTrip(id: String, request: PatchRequestDto): SavedTripDto
    suspend fun deleteSavedTrip(id: String)
    suspend fun registerPushToken(request: RegisterPushTokenRequestDto): PushTokenRegistrationDto
    suspend fun updatePushToken(id: String, request: PatchRequestDto): PushTokenRegistrationDto
    suspend fun deletePushToken(id: String)
    suspend fun getSubscription(): SubscriptionDto
    suspend fun uploadProfileImage(image: PreparedProfileImage): UploadProfileImageResponseDto
}
