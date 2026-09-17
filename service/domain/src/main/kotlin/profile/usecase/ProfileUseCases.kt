package profile.usecase

import profile.model.CreateSavedPlaceCommand
import profile.model.CreateSavedTripCommand
import profile.model.ProfileImageReference
import profile.model.RegisterPushTokenCommand
import profile.model.SavedPlacesQuery
import profile.model.SavedTripsQuery
import profile.model.UpdatePreferencesCommand
import profile.model.UpdateProfileCommand
import profile.model.UpdatePushTokenCommand
import profile.model.UpdateSavedPlaceCommand
import profile.model.UpdateSavedTripCommand
import profile.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke() = repository.getProfile()
}

class UpdateProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(command: UpdateProfileCommand) = repository.updateProfile(command)
}

class GetPreferencesUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke() = repository.getPreferences()
}

class UpdatePreferencesUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(command: UpdatePreferencesCommand) = repository.updatePreferences(command)
}

class GetSavedPlacesUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(query: SavedPlacesQuery = SavedPlacesQuery()) = repository.getSavedPlaces(query)
}

class CreateSavedPlaceUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(command: CreateSavedPlaceCommand) = repository.createSavedPlace(command)
}

class UpdateSavedPlaceUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(id: String, command: UpdateSavedPlaceCommand) = repository.updateSavedPlace(id, command)
}

class DeleteSavedPlaceUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(id: String) = repository.deleteSavedPlace(id)
}

class GetSavedTripsUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(query: SavedTripsQuery = SavedTripsQuery()) = repository.getSavedTrips(query)
}

class CreateSavedTripUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(command: CreateSavedTripCommand) = repository.createSavedTrip(command)
}

class UpdateSavedTripUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(id: String, command: UpdateSavedTripCommand) = repository.updateSavedTrip(id, command)
}

class DeleteSavedTripUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(id: String) = repository.deleteSavedTrip(id)
}

class RegisterPushTokenUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(command: RegisterPushTokenCommand) = repository.registerPushToken(command)
}

class UpdatePushTokenUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(id: String, command: UpdatePushTokenCommand) = repository.updatePushToken(id, command)
}

class DeletePushTokenUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(id: String) = repository.deletePushToken(id)
}

class GetSubscriptionUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke() = repository.getSubscription()
}

class UploadProfileImageUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(reference: ProfileImageReference) = repository.uploadProfileImage(reference)
}
