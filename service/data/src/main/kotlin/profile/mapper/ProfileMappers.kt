package profile.mapper

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import profile.model.CreateSavedPlaceCommand
import profile.model.CreateSavedPlaceRequestDto
import profile.model.CreateSavedTripCommand
import profile.model.CreateSavedTripRequestDto
import profile.model.DistanceUnit
import profile.model.Page
import profile.model.PatchField
import profile.model.PatchRequestDto
import profile.model.Profile
import profile.model.ProfileDto
import profile.model.ProfilePreferences
import profile.model.ProfilePreferencesDto
import profile.model.PushPlatform
import profile.model.PushTokenRegistration
import profile.model.PushTokenRegistrationDto
import profile.model.RegisterPushTokenCommand
import profile.model.RegisterPushTokenRequestDto
import profile.model.SavedPlace
import profile.model.SavedPlaceDto
import profile.model.SavedPlacesPageDto
import profile.model.SavedTrip
import profile.model.SavedTripDto
import profile.model.SavedTripsPageDto
import profile.model.Subscription
import profile.model.SubscriptionDto
import profile.model.SubscriptionPlan
import profile.model.SubscriptionProvider
import profile.model.SubscriptionStatus
import profile.model.TemperatureUnit
import profile.model.TripStatus
import profile.model.UpdatePreferencesCommand
import profile.model.UpdateProfileCommand
import profile.model.UpdatePushTokenCommand
import profile.model.UpdateSavedPlaceCommand
import profile.model.UpdateSavedTripCommand

fun ProfileDto.toDomain() = Profile(userId, displayName, avatarPath, createdAt, updatedAt)

fun ProfilePreferencesDto.toDomain() = ProfilePreferences(
    userId = userId,
    languageCode = languageCode,
    distanceUnit = distanceUnit.toDistanceUnit(),
    temperatureUnit = temperatureUnit.toTemperatureUnit(),
    currencyCode = currencyCode,
    timeZone = timeZone,
    flightNotifications = flightNotifications,
    tripReminders = tripReminders,
    marketingNotifications = marketingNotifications,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun SavedPlaceDto.toDomain() = SavedPlace(
    id, userId, provider, providerPlaceId, name, address, countryCode,
    latitude, longitude, notes, createdAt, updatedAt,
)

fun SavedTripDto.toDomain() = SavedTrip(
    id, ownerId, title, destinationName, startDate, endDate,
    status.toTripStatus(), isArchived, createdAt, updatedAt,
)

fun PushTokenRegistrationDto.toDomain() = PushTokenRegistration(
    id, userId, token, platform.toPushPlatform(), deviceName, enabled, createdAt, updatedAt,
)

fun SubscriptionDto.toDomain() = Subscription(
    userId = userId,
    provider = provider.toSubscriptionProvider(),
    productId = productId,
    plan = planCode.toSubscriptionPlan(),
    status = status.toSubscriptionStatus(),
    currentPeriodEnd = currentPeriodEnd,
    providerSubscriptionId = providerSubscriptionId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun SavedPlacesPageDto.toDomain() = Page(
    items = items.map(SavedPlaceDto::toDomain),
    limit = pagination.limit,
    offset = pagination.offset,
    count = pagination.count,
)

fun SavedTripsPageDto.toDomain() = Page(
    items = items.map(SavedTripDto::toDomain),
    limit = pagination.limit,
    offset = pagination.offset,
    count = pagination.count,
)

fun CreateSavedPlaceCommand.toDto() = CreateSavedPlaceRequestDto(
    name = name,
    provider = provider,
    providerPlaceId = providerPlaceId,
    address = address,
    countryCode = countryCode,
    latitude = latitude,
    longitude = longitude,
    notes = notes,
)

fun CreateSavedTripCommand.toDto() = CreateSavedTripRequestDto(
    title = title,
    destinationName = destinationName,
    startDate = startDate,
    endDate = endDate,
    status = status?.toWire(),
    isArchived = archived,
)

fun RegisterPushTokenCommand.toDto() = RegisterPushTokenRequestDto(
    token = token,
    platform = platform.toWire(),
    deviceName = deviceName,
    enabled = enabled,
)

fun UpdateProfileCommand.toDto() = PatchRequestDto(buildJsonObject {
    putStringPatch("display_name", displayName)
})

fun UpdatePreferencesCommand.toDto() = PatchRequestDto(buildJsonObject {
    putStringPatch("language_code", languageCode)
    putMappedPatch("distance_unit", distanceUnit) { it.toWire() }
    putMappedPatch("temperature_unit", temperatureUnit) { it.toWire() }
    putStringPatch("currency_code", currencyCode)
    putStringPatch("time_zone", timeZone)
    putBooleanPatch("flight_notifications", flightNotifications)
    putBooleanPatch("trip_reminders", tripReminders)
    putBooleanPatch("marketing_notifications", marketingNotifications)
})

fun UpdateSavedPlaceCommand.toDto() = PatchRequestDto(buildJsonObject {
    putStringPatch("provider", provider)
    putStringPatch("provider_place_id", providerPlaceId)
    putStringPatch("name", name)
    putStringPatch("address", address)
    putStringPatch("country_code", countryCode)
    putDoublePatch("latitude", latitude)
    putDoublePatch("longitude", longitude)
    putStringPatch("notes", notes)
})

fun UpdateSavedTripCommand.toDto() = PatchRequestDto(buildJsonObject {
    putStringPatch("title", title)
    putStringPatch("destination_name", destinationName)
    putStringPatch("start_date", startDate)
    putStringPatch("end_date", endDate)
    putMappedPatch("status", status) { it.toWire() }
    putBooleanPatch("is_archived", archived)
})

fun UpdatePushTokenCommand.toDto() = PatchRequestDto(buildJsonObject {
    putStringPatch("device_name", deviceName)
    putBooleanPatch("enabled", enabled)
})

private fun kotlinx.serialization.json.JsonObjectBuilder.putStringPatch(key: String, value: PatchField<String?>) {
    if (value is PatchField.Supplied) put(key, value.value)
}

private fun kotlinx.serialization.json.JsonObjectBuilder.putBooleanPatch(key: String, value: PatchField<Boolean>) {
    if (value is PatchField.Supplied) put(key, value.value)
}

private fun kotlinx.serialization.json.JsonObjectBuilder.putDoublePatch(key: String, value: PatchField<Double?>) {
    if (value is PatchField.Supplied) put(key, value.value)
}

private fun <T> kotlinx.serialization.json.JsonObjectBuilder.putMappedPatch(
    key: String,
    value: PatchField<T>,
    transform: (T) -> String,
) {
    if (value is PatchField.Supplied) put(key, transform(value.value))
}

fun DistanceUnit.toWire() = when (this) {
    DistanceUnit.Kilometers -> "km"
    DistanceUnit.Miles -> "mile"
}

fun TemperatureUnit.toWire() = when (this) {
    TemperatureUnit.Celsius -> "celsius"
    TemperatureUnit.Fahrenheit -> "fahrenheit"
}

fun TripStatus.toWire() = name.lowercase()

fun PushPlatform.toWire() = name.lowercase()

private fun String.toDistanceUnit() = when (this) {
    "km" -> DistanceUnit.Kilometers
    "mile" -> DistanceUnit.Miles
    else -> unknown("distance unit")
}

private fun String.toTemperatureUnit() = when (this) {
    "celsius" -> TemperatureUnit.Celsius
    "fahrenheit" -> TemperatureUnit.Fahrenheit
    else -> unknown("temperature unit")
}

private fun String.toTripStatus() = when (this) {
    "draft" -> TripStatus.Draft
    "upcoming" -> TripStatus.Upcoming
    "active" -> TripStatus.Active
    "completed" -> TripStatus.Completed
    "cancelled" -> TripStatus.Cancelled
    else -> unknown("trip status")
}

private fun String.toPushPlatform() = when (this) {
    "android" -> PushPlatform.Android
    "ios" -> PushPlatform.Ios
    "web" -> PushPlatform.Web
    else -> unknown("push platform")
}

private fun String.toSubscriptionProvider() = when (this) {
    "google_play" -> SubscriptionProvider.GooglePlay
    "app_store" -> SubscriptionProvider.AppStore
    "manual" -> SubscriptionProvider.Manual
    else -> unknown("subscription provider")
}

private fun String.toSubscriptionPlan() = when (this) {
    "free" -> SubscriptionPlan.Free
    "premium" -> SubscriptionPlan.Premium
    else -> unknown("subscription plan")
}

private fun String.toSubscriptionStatus() = when (this) {
    "free" -> SubscriptionStatus.Free
    "trialing" -> SubscriptionStatus.Trialing
    "active" -> SubscriptionStatus.Active
    "past_due" -> SubscriptionStatus.PastDue
    "cancelled" -> SubscriptionStatus.Cancelled
    "expired" -> SubscriptionStatus.Expired
    else -> unknown("subscription status")
}

private fun unknown(type: String): Nothing = throw SerializationException("Unknown $type")
