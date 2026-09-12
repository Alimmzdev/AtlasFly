package profile.repository

import dev.alimmz.atlasfly.core.network.AuthTokenException
import dev.alimmz.atlasfly.core.network.NetworkClientException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import profile.datasource.remote.ProfileRemoteDataSource
import profile.mapper.toDomain
import profile.mapper.toDto
import profile.mapper.toWire
import profile.model.ApiError
import profile.model.ApiResult
import profile.model.CreateSavedPlaceCommand
import profile.model.CreateSavedTripCommand
import profile.model.Page
import profile.model.PatchField
import profile.model.Profile
import profile.model.ProfileImagePath
import profile.model.ProfileImageReference
import profile.model.ProfileImageResolver
import profile.model.ProfilePreferences
import profile.model.ProfileValidationException
import profile.model.PushTokenRegistration
import profile.model.RegisterPushTokenCommand
import profile.model.RemoteHttpException
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ProfileRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProfileRemoteDataSource,
    private val imageResolver: ProfileImageResolver,
    private val json: Json,
) : ProfileRepository {

    override fun getProfile(): Flow<ApiResult<Profile>> = apiFlow {
        remoteDataSource.getProfile().toDomain()
    }

    override fun updateProfile(command: UpdateProfileCommand): Flow<ApiResult<Profile>> = apiFlow {
        validateProfile(command)
        remoteDataSource.updateProfile(command.toDto()).toDomain()
    }

    override fun getPreferences(): Flow<ApiResult<ProfilePreferences>> = apiFlow {
        remoteDataSource.getPreferences().toDomain()
    }

    override fun updatePreferences(
        command: UpdatePreferencesCommand,
    ): Flow<ApiResult<ProfilePreferences>> = apiFlow {
        validatePreferences(command)
        remoteDataSource.updatePreferences(command.toDto()).toDomain()
    }

    override fun getSavedPlaces(query: SavedPlacesQuery): Flow<ApiResult<Page<SavedPlace>>> = apiFlow {
        validatePagination(query.limit, query.offset)
        val countryCode = query.countryCode?.normalizedCountryCode()
        remoteDataSource.getSavedPlaces(query.limit, query.offset, countryCode).toDomain()
    }

    override fun createSavedPlace(
        command: CreateSavedPlaceCommand,
    ): Flow<ApiResult<SavedPlace>> = apiFlow {
        validateSavedPlace(command)
        remoteDataSource.createSavedPlace(
            command.copy(countryCode = command.countryCode?.normalizedCountryCode()).toDto(),
        ).toDomain()
    }

    override fun updateSavedPlace(
        id: String,
        command: UpdateSavedPlaceCommand,
    ): Flow<ApiResult<SavedPlace>> = apiFlow {
        validateResourceId(id)
        validateSavedPlace(command)
        val normalized = command.copy(
            countryCode = command.countryCode.mapSupplied { it?.normalizedCountryCode() },
        )
        remoteDataSource.updateSavedPlace(id, normalized.toDto()).toDomain()
    }

    override fun deleteSavedPlace(id: String): Flow<ApiResult<Unit>> = apiFlow {
        validateResourceId(id)
        remoteDataSource.deleteSavedPlace(id)
    }

    override fun getSavedTrips(query: SavedTripsQuery): Flow<ApiResult<Page<SavedTrip>>> = apiFlow {
        validatePagination(query.limit, query.offset)
        remoteDataSource.getSavedTrips(
            limit = query.limit,
            offset = query.offset,
            status = query.status?.toWire(),
            archived = query.archived,
        ).toDomain()
    }

    override fun createSavedTrip(command: CreateSavedTripCommand): Flow<ApiResult<SavedTrip>> = apiFlow {
        validateSavedTrip(command)
        remoteDataSource.createSavedTrip(command.toDto()).toDomain()
    }

    override fun updateSavedTrip(
        id: String,
        command: UpdateSavedTripCommand,
    ): Flow<ApiResult<SavedTrip>> = apiFlow {
        validateResourceId(id)
        validateSavedTrip(command)
        remoteDataSource.updateSavedTrip(id, command.toDto()).toDomain()
    }

    override fun deleteSavedTrip(id: String): Flow<ApiResult<Unit>> = apiFlow {
        validateResourceId(id)
        remoteDataSource.deleteSavedTrip(id)
    }

    override fun registerPushToken(
        command: RegisterPushTokenCommand,
    ): Flow<ApiResult<PushTokenRegistration>> = apiFlow {
        if (command.token.isBlank()) invalid(FIELD_TOKEN)
        remoteDataSource.registerPushToken(command.toDto()).toDomain()
    }

    override fun updatePushToken(
        id: String,
        command: UpdatePushTokenCommand,
    ): Flow<ApiResult<PushTokenRegistration>> = apiFlow {
        validateResourceId(id)
        if (command.toDto().body.isEmpty()) invalid()
        remoteDataSource.updatePushToken(id, command.toDto()).toDomain()
    }

    override fun deletePushToken(id: String): Flow<ApiResult<Unit>> = apiFlow {
        validateResourceId(id)
        remoteDataSource.deletePushToken(id)
    }

    override fun getSubscription(): Flow<ApiResult<Subscription>> = apiFlow {
        remoteDataSource.getSubscription().toDomain()
    }

    override fun uploadProfileImage(
        reference: ProfileImageReference,
    ): Flow<ApiResult<ProfileImagePath>> = apiFlow {
        if (reference.contentUri.isBlank()) invalid(ProfileImageResolver.FIELD_IMAGE)
        val response = remoteDataSource.uploadProfileImage(imageResolver.prepare(reference))
        ProfileImagePath(response.path)
    }

    private fun <T> apiFlow(block: suspend () -> T): Flow<ApiResult<T>> = flow {
        emit(ApiResult.Loading)
        try {
            emit(ApiResult.Success(block()))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            emit(ApiResult.Failure(error.toApiError()))
        }
    }.flowOn(Dispatchers.IO)

    private fun Throwable.toApiError(): ApiError = when (this) {
        is AuthTokenException.MissingUser,
        is AuthTokenException.MissingToken -> ApiError.MissingAuthentication
        is AuthTokenException.UserChanged -> ApiError.Unauthorized
        is AuthTokenException.Network -> ApiError.Network
        is AuthTokenException.Unknown -> ApiError.Unknown(cause)
        is NetworkClientException.Configuration -> ApiError.Configuration
        is NetworkClientException.Connectivity -> ApiError.Network
        is ProfileValidationException -> ApiError.Validation(field)
        is RemoteHttpException -> toApiError()
        is SerializationException -> ApiError.Serialization
        is IOException -> ApiError.Network
        else -> ApiError.Unknown(this)
    }

    private fun RemoteHttpException.toApiError(): ApiError {
        val validationField = parseValidationField(responseBody)
        return when (status) {
            400, 409, 422 -> ApiError.Validation(validationField)
            413, 415 -> ApiError.Validation(ProfileImageResolver.FIELD_IMAGE)
            401 -> ApiError.Unauthorized
            403 -> ApiError.Forbidden
            404 -> ApiError.NotFound
            429 -> ApiError.RateLimited(retryAfterSeconds)
            in 500..599 -> ApiError.Server
            else -> ApiError.Unknown()
        }
    }

    private fun parseValidationField(body: String?): String? {
        if (body.isNullOrBlank()) return null
        val message = runCatching {
            json.parseToJsonElement(body).jsonObject["error"]?.jsonPrimitive?.content
        }.getOrNull()
        return VALIDATION_FIELDS.firstOrNull { field ->
            message == "$field is required"
        }
    }

    private fun validateProfile(command: UpdateProfileCommand) {
        val request = command.toDto().body
        if (request.isEmpty()) invalid()
        val name = (command.displayName as? PatchField.Supplied)?.value ?: return
        val codePoints = name.codePointCount(0, name.length)
        if (codePoints !in 1..100) invalid(FIELD_DISPLAY_NAME)
    }

    private fun validatePreferences(command: UpdatePreferencesCommand) {
        if (command.toDto().body.isEmpty()) invalid()
        command.languageCode.suppliedValueOrNull()?.let {
            if (!LANGUAGE_CODE.matches(it)) invalid(FIELD_LANGUAGE_CODE)
        }
        command.currencyCode.suppliedValueOrNull()?.let {
            if (!CURRENCY_CODE.matches(it)) invalid(FIELD_CURRENCY_CODE)
        }
    }

    private fun validateSavedPlace(command: CreateSavedPlaceCommand) {
        if (command.name.trim().isEmpty()) invalid(FIELD_NAME)
        command.countryCode?.normalizedCountryCode()
        validateCoordinates(command.latitude, command.longitude, requirePair = true)
    }

    private fun validateSavedPlace(command: UpdateSavedPlaceCommand) {
        if (command.toDto().body.isEmpty()) invalid()
        command.name.suppliedValueOrNull()?.let {
            if (it.trim().isEmpty()) invalid(FIELD_NAME)
        }
        (command.countryCode as? PatchField.Supplied)?.value?.normalizedCountryCode()
        validateLatitude((command.latitude as? PatchField.Supplied)?.value)
        validateLongitude((command.longitude as? PatchField.Supplied)?.value)
    }

    private fun validateCoordinates(latitude: Double?, longitude: Double?, requirePair: Boolean) {
        if (requirePair && (latitude == null) != (longitude == null)) invalid(FIELD_COORDINATES)
        validateLatitude(latitude)
        validateLongitude(longitude)
    }

    private fun validateLatitude(value: Double?) {
        if (value != null && (!value.isFinite() || value !in -90.0..90.0)) invalid(FIELD_LATITUDE)
    }

    private fun validateLongitude(value: Double?) {
        if (value != null && (!value.isFinite() || value !in -180.0..180.0)) invalid(FIELD_LONGITUDE)
    }

    private fun validateSavedTrip(command: CreateSavedTripCommand) {
        if (command.title.trim().isEmpty()) invalid(FIELD_TITLE)
        if (command.destinationName.trim().isEmpty()) invalid(FIELD_DESTINATION)
        validateDates(command.startDate, command.endDate)
    }

    private fun validateSavedTrip(command: UpdateSavedTripCommand) {
        if (command.toDto().body.isEmpty()) invalid()
        command.title.suppliedValueOrNull()?.let {
            if (it.trim().isEmpty()) invalid(FIELD_TITLE)
        }
        command.destinationName.suppliedValueOrNull()?.let {
            if (it.trim().isEmpty()) invalid(FIELD_DESTINATION)
        }
        val start = (command.startDate as? PatchField.Supplied)?.value
        val end = (command.endDate as? PatchField.Supplied)?.value
        start?.let(::validateDate)
        end?.let(::validateDate)
        if (start != null && end != null && end < start) invalid(FIELD_END_DATE)
    }

    private fun validateDates(startDate: String?, endDate: String?) {
        startDate?.let(::validateDate)
        endDate?.let(::validateDate)
        if (startDate != null && endDate != null && endDate < startDate) invalid(FIELD_END_DATE)
    }

    private fun validateDate(value: String) {
        val formatter = SimpleDateFormat(ISO_DATE_PATTERN, Locale.ROOT).apply { isLenient = false }
        val parsed = runCatching { formatter.parse(value) }.getOrNull()
        if (parsed == null || formatter.format(parsed) != value) invalid(FIELD_DATE)
    }

    private fun validatePagination(limit: Int?, offset: Int?) {
        if (limit != null && limit !in 1..100) invalid(FIELD_LIMIT)
        if (offset != null && offset < 0) invalid(FIELD_OFFSET)
    }

    private fun validateResourceId(id: String) {
        if (!UUID.matches(id)) invalid(FIELD_ID)
    }

    private fun String.normalizedCountryCode(): String {
        val normalized = uppercase(Locale.ROOT)
        if (!COUNTRY_CODE.matches(normalized)) invalid(FIELD_COUNTRY_CODE)
        return normalized
    }

    private fun <T> PatchField<T>.suppliedValueOrNull(): T? =
        (this as? PatchField.Supplied)?.value

    private fun <T> PatchField<T>.mapSupplied(transform: (T) -> T): PatchField<T> = when (this) {
        PatchField.Omitted -> PatchField.Omitted
        is PatchField.Supplied -> PatchField.Supplied(transform(value))
    }

    private fun invalid(field: String? = null): Nothing = throw ProfileValidationException(field)

    private companion object {
        val UUID = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
        val COUNTRY_CODE = Regex("^[A-Z]{2}$")
        val LANGUAGE_CODE = Regex("^[a-z]{2}(-[A-Z]{2})?$")
        val CURRENCY_CODE = Regex("^[A-Z]{3}$")
        const val ISO_DATE_PATTERN = "yyyy-MM-dd"
        const val FIELD_ID = "id"
        const val FIELD_LIMIT = "limit"
        const val FIELD_OFFSET = "offset"
        const val FIELD_DISPLAY_NAME = "display_name"
        const val FIELD_LANGUAGE_CODE = "language_code"
        const val FIELD_CURRENCY_CODE = "currency_code"
        const val FIELD_NAME = "name"
        const val FIELD_COUNTRY_CODE = "country_code"
        const val FIELD_COORDINATES = "coordinates"
        const val FIELD_LATITUDE = "latitude"
        const val FIELD_LONGITUDE = "longitude"
        const val FIELD_TITLE = "title"
        const val FIELD_DESTINATION = "destination_name"
        const val FIELD_DATE = "date"
        const val FIELD_END_DATE = "end_date"
        const val FIELD_TOKEN = "token"
        val VALIDATION_FIELDS = setOf(FIELD_NAME, FIELD_TITLE, FIELD_DESTINATION, FIELD_TOKEN)
    }
}
