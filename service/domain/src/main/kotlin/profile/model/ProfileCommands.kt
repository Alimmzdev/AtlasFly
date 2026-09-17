package profile.model

data class UpdateProfileCommand(
    val displayName: PatchField<String?> = PatchField.Omitted,
)

data class UpdatePreferencesCommand(
    val languageCode: PatchField<String> = PatchField.Omitted,
    val distanceUnit: PatchField<DistanceUnit> = PatchField.Omitted,
    val temperatureUnit: PatchField<TemperatureUnit> = PatchField.Omitted,
    val currencyCode: PatchField<String> = PatchField.Omitted,
    val timeZone: PatchField<String> = PatchField.Omitted,
    val flightNotifications: PatchField<Boolean> = PatchField.Omitted,
    val tripReminders: PatchField<Boolean> = PatchField.Omitted,
    val marketingNotifications: PatchField<Boolean> = PatchField.Omitted,
)

data class SavedPlacesQuery(
    val limit: Int? = null,
    val offset: Int? = null,
    val countryCode: String? = null,
)

data class CreateSavedPlaceCommand(
    val name: String,
    val provider: String? = null,
    val providerPlaceId: String? = null,
    val address: String? = null,
    val countryCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String? = null,
)

data class UpdateSavedPlaceCommand(
    val provider: PatchField<String> = PatchField.Omitted,
    val providerPlaceId: PatchField<String?> = PatchField.Omitted,
    val name: PatchField<String> = PatchField.Omitted,
    val address: PatchField<String?> = PatchField.Omitted,
    val countryCode: PatchField<String?> = PatchField.Omitted,
    val latitude: PatchField<Double?> = PatchField.Omitted,
    val longitude: PatchField<Double?> = PatchField.Omitted,
    val notes: PatchField<String?> = PatchField.Omitted,
)

data class SavedTripsQuery(
    val limit: Int? = null,
    val offset: Int? = null,
    val status: TripStatus? = null,
    val archived: Boolean? = null,
)

data class CreateSavedTripCommand(
    val title: String,
    val destinationName: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: TripStatus? = null,
    val archived: Boolean? = null,
)

data class UpdateSavedTripCommand(
    val title: PatchField<String> = PatchField.Omitted,
    val destinationName: PatchField<String> = PatchField.Omitted,
    val startDate: PatchField<String?> = PatchField.Omitted,
    val endDate: PatchField<String?> = PatchField.Omitted,
    val status: PatchField<TripStatus> = PatchField.Omitted,
    val archived: PatchField<Boolean> = PatchField.Omitted,
)

data class RegisterPushTokenCommand(
    val token: String,
    val platform: PushPlatform = PushPlatform.Android,
    val deviceName: String? = null,
    val enabled: Boolean = true,
)

data class UpdatePushTokenCommand(
    val deviceName: PatchField<String?> = PatchField.Omitted,
    val enabled: PatchField<Boolean> = PatchField.Omitted,
)

@JvmInline
value class ProfileImageReference(val contentUri: String)
