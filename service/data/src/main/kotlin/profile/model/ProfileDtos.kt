package profile.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String?,
    @SerialName("avatar_path") val avatarPath: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class ProfilePreferencesDto(
    @SerialName("user_id") val userId: String,
    @SerialName("language_code") val languageCode: String,
    @SerialName("distance_unit") val distanceUnit: String,
    @SerialName("temperature_unit") val temperatureUnit: String,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("time_zone") val timeZone: String,
    @SerialName("flight_notifications") val flightNotifications: Boolean,
    @SerialName("trip_reminders") val tripReminders: Boolean,
    @SerialName("marketing_notifications") val marketingNotifications: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class SavedPlaceDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val provider: String,
    @SerialName("provider_place_id") val providerPlaceId: String?,
    val name: String,
    val address: String?,
    @SerialName("country_code") val countryCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val notes: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class SavedTripDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val title: String,
    @SerialName("destination_name") val destinationName: String,
    @SerialName("start_date") val startDate: String?,
    @SerialName("end_date") val endDate: String?,
    val status: String,
    @SerialName("is_archived") val isArchived: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class PushTokenRegistrationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val token: String,
    val platform: String,
    @SerialName("device_name") val deviceName: String?,
    val enabled: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class SubscriptionDto(
    @SerialName("user_id") val userId: String,
    val provider: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("plan_code") val planCode: String,
    val status: String,
    @SerialName("current_period_end") val currentPeriodEnd: String?,
    @SerialName("provider_subscription_id") val providerSubscriptionId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class PaginationDto(
    val limit: Int,
    val offset: Int,
    val count: Int,
)

@Serializable
data class SavedPlacesPageDto(
    val items: List<SavedPlaceDto>,
    val pagination: PaginationDto,
)

@Serializable
data class SavedTripsPageDto(
    val items: List<SavedTripDto>,
    val pagination: PaginationDto,
)

@Serializable
data class UploadProfileImageResponseDto(val path: String)

@Serializable
data class CreateSavedPlaceRequestDto(
    val name: String,
    val provider: String? = null,
    @SerialName("provider_place_id") val providerPlaceId: String? = null,
    val address: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String? = null,
)

@Serializable
data class CreateSavedTripRequestDto(
    val title: String,
    @SerialName("destination_name") val destinationName: String,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val status: String? = null,
    @SerialName("is_archived") val isArchived: Boolean? = null,
)

@Serializable
data class RegisterPushTokenRequestDto(
    val token: String,
    val platform: String,
    @SerialName("device_name") val deviceName: String? = null,
    val enabled: Boolean,
)

@JvmInline
value class PatchRequestDto(val body: JsonObject)
