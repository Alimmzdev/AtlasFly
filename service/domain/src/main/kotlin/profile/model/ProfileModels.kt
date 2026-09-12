package profile.model

data class Profile(
    val userId: String,
    val displayName: String?,
    val avatarPath: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class ProfilePreferences(
    val userId: String,
    val languageCode: String,
    val distanceUnit: DistanceUnit,
    val temperatureUnit: TemperatureUnit,
    val currencyCode: String,
    val timeZone: String,
    val flightNotifications: Boolean,
    val tripReminders: Boolean,
    val marketingNotifications: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

enum class DistanceUnit { Kilometers, Miles }

enum class TemperatureUnit { Celsius, Fahrenheit }

data class SavedPlace(
    val id: String,
    val userId: String,
    val provider: String,
    val providerPlaceId: String?,
    val name: String,
    val address: String?,
    val countryCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class SavedTrip(
    val id: String,
    val ownerId: String,
    val title: String,
    val destinationName: String,
    val startDate: String?,
    val endDate: String?,
    val status: TripStatus,
    val isArchived: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

enum class TripStatus { Draft, Upcoming, Active, Completed, Cancelled }

data class PushTokenRegistration(
    val id: String,
    val userId: String,
    val token: String,
    val platform: PushPlatform,
    val deviceName: String?,
    val enabled: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

enum class PushPlatform { Android, Ios, Web }

data class Subscription(
    val userId: String,
    val provider: SubscriptionProvider,
    val productId: String?,
    val plan: SubscriptionPlan,
    val status: SubscriptionStatus,
    val currentPeriodEnd: String?,
    val providerSubscriptionId: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

enum class SubscriptionProvider { GooglePlay, AppStore, Manual }

enum class SubscriptionPlan { Free, Premium }

enum class SubscriptionStatus { Free, Trialing, Active, PastDue, Cancelled, Expired }

data class Page<T>(
    val items: List<T>,
    val limit: Int,
    val offset: Int,
    val count: Int,
)

@JvmInline
value class ProfileImagePath(val value: String)
