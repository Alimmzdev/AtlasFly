package dev.alimmz.atlasfly.feature.profile.presentation

import androidx.annotation.StringRes
import profile.model.ApiError

sealed interface ProfileNotice {
    data object ProfileSaved : ProfileNotice
    data object ImageUploaded : ProfileNotice
    data object PreferencesSaved : ProfileNotice
    data object PlaceCreated : ProfileNotice
    data object PlaceUpdated : ProfileNotice
    data object PlaceDeleted : ProfileNotice
    data object TripCreated : ProfileNotice
    data object TripUpdated : ProfileNotice
    data object TripDeleted : ProfileNotice
}

@StringRes
internal fun ProfileNotice.messageRes(): Int = when (this) {
    ProfileNotice.ProfileSaved -> R.string.profile_saved_message
    ProfileNotice.ImageUploaded -> R.string.profile_image_uploaded
    ProfileNotice.PreferencesSaved -> R.string.profile_preferences_saved
    ProfileNotice.PlaceCreated -> R.string.profile_place_created
    ProfileNotice.PlaceUpdated -> R.string.profile_place_updated
    ProfileNotice.PlaceDeleted -> R.string.profile_place_deleted
    ProfileNotice.TripCreated -> R.string.profile_trip_created
    ProfileNotice.TripUpdated -> R.string.profile_trip_updated
    ProfileNotice.TripDeleted -> R.string.profile_trip_deleted
}

@StringRes
internal fun ApiError.messageRes(): Int = when (this) {
    ApiError.MissingAuthentication,
    ApiError.Unauthorized -> R.string.profile_error_authentication
    ApiError.Forbidden -> R.string.profile_error_forbidden
    is ApiError.Validation -> when (field) {
        "image" -> R.string.profile_error_image
        else -> R.string.profile_error_validation
    }
    ApiError.NotFound -> R.string.profile_error_not_found
    is ApiError.RateLimited -> R.string.profile_error_rate_limited
    ApiError.Server -> R.string.profile_error_server
    ApiError.Network -> R.string.profile_error_network
    ApiError.Serialization -> R.string.profile_error_response
    ApiError.Configuration -> R.string.profile_error_configuration
    is ApiError.Unknown -> R.string.profile_error_unknown
}
