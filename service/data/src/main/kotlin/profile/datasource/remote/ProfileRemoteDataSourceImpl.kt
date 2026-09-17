package profile.datasource.remote

import dev.alimmz.atlasfly.core.network.AtlasFlyApiClient
import dev.alimmz.atlasfly.core.network.AtlasFlyHttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.content.TextContent
import io.ktor.utils.io.streams.asInput
import kotlinx.serialization.json.Json
import profile.model.CreateSavedPlaceRequestDto
import profile.model.CreateSavedTripRequestDto
import profile.model.PatchRequestDto
import profile.model.PreparedProfileImage
import profile.model.ProfileDto
import profile.model.ProfileImageResolver
import profile.model.ProfilePreferencesDto
import profile.model.PushTokenRegistrationDto
import profile.model.RegisterPushTokenRequestDto
import profile.model.RemoteHttpException
import profile.model.SavedPlaceDto
import profile.model.SavedPlacesPageDto
import profile.model.SavedTripDto
import profile.model.SavedTripsPageDto
import profile.model.SubscriptionDto
import profile.model.UploadProfileImageResponseDto
import javax.inject.Inject

class ProfileRemoteDataSourceImpl @Inject constructor(
    private val apiClient: AtlasFlyApiClient,
    private val json: Json,
    private val imageResolver: ProfileImageResolver,
) : ProfileRemoteDataSource {

    override suspend fun getProfile(): ProfileDto = get(PROFILE)

    override suspend fun updateProfile(request: PatchRequestDto): ProfileDto =
        patch(PROFILE, request)

    override suspend fun getPreferences(): ProfilePreferencesDto = get(PREFERENCES)

    override suspend fun updatePreferences(request: PatchRequestDto): ProfilePreferencesDto =
        patch(PREFERENCES, request)

    override suspend fun getSavedPlaces(
        limit: Int?,
        offset: Int?,
        countryCode: String?,
    ): SavedPlacesPageDto = get(
        path = SAVED_PLACES,
        parameters = Parameters.build {
            limit?.let { append("limit", it.toString()) }
            offset?.let { append("offset", it.toString()) }
            countryCode?.let { append("country_code", it) }
        },
    )

    override suspend fun createSavedPlace(request: CreateSavedPlaceRequestDto): SavedPlaceDto =
        post(SAVED_PLACES, request)

    override suspend fun updateSavedPlace(id: String, request: PatchRequestDto): SavedPlaceDto =
        patch(listOf(SAVED_PLACES, id), request)

    override suspend fun deleteSavedPlace(id: String) = delete(SAVED_PLACES, id)

    override suspend fun getSavedTrips(
        limit: Int?,
        offset: Int?,
        status: String?,
        archived: Boolean?,
    ): SavedTripsPageDto = get(
        path = SAVED_TRIPS,
        parameters = Parameters.build {
            limit?.let { append("limit", it.toString()) }
            offset?.let { append("offset", it.toString()) }
            status?.let { append("status", it) }
            archived?.let { append("archived", it.toString()) }
        },
    )

    override suspend fun createSavedTrip(request: CreateSavedTripRequestDto): SavedTripDto =
        post(SAVED_TRIPS, request)

    override suspend fun updateSavedTrip(id: String, request: PatchRequestDto): SavedTripDto =
        patch(listOf(SAVED_TRIPS, id), request)

    override suspend fun deleteSavedTrip(id: String) = delete(SAVED_TRIPS, id)

    override suspend fun registerPushToken(
        request: RegisterPushTokenRequestDto,
    ): PushTokenRegistrationDto = post(PUSH_TOKENS, request)

    override suspend fun updatePushToken(
        id: String,
        request: PatchRequestDto,
    ): PushTokenRegistrationDto = patch(listOf(PUSH_TOKENS, id), request)

    override suspend fun deletePushToken(id: String) = delete(PUSH_TOKENS, id)

    override suspend fun getSubscription(): SubscriptionDto = get(SUBSCRIPTION)

    override suspend fun uploadProfileImage(
        image: PreparedProfileImage,
    ): UploadProfileImageResponseDto {
        val response = apiClient.execute(
            method = HttpMethod.Post,
            pathSegments = listOf(UPLOAD_PROFILE_IMAGE),
            bodyFactory = {
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "image",
                            value = InputProvider(image.size) {
                                imageResolver.open(image.uri).asInput()
                            },
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, image.mimeType)
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=\"${image.fileName}\"",
                                )
                            },
                        )
                    },
                )
            },
        )
        return decodeSuccessful(response)
    }

    private suspend inline fun <reified T> get(
        path: String,
        parameters: Parameters = Parameters.Empty,
    ): T = decodeSuccessful(
        apiClient.execute(HttpMethod.Get, listOf(path), parameters),
    )

    private suspend inline fun <reified Response, reified Request> post(
        path: String,
        request: Request,
    ): Response = decodeSuccessful(
        apiClient.execute(
            method = HttpMethod.Post,
            pathSegments = listOf(path),
            bodyFactory = { jsonBody(json.encodeToString(request)) },
        ),
    )

    private suspend inline fun <reified T> patch(path: String, request: PatchRequestDto): T =
        patch(listOf(path), request)

    private suspend inline fun <reified T> patch(
        path: List<String>,
        request: PatchRequestDto,
    ): T = decodeSuccessful(
        apiClient.execute(
            method = HttpMethod.Patch,
            pathSegments = path,
            bodyFactory = { jsonBody(request.body.toString()) },
        ),
    )

    private suspend fun delete(path: String, id: String) {
        ensureSuccessful(
            apiClient.execute(HttpMethod.Delete, listOf(path, id)),
        )
    }

    private inline fun <reified T> decodeSuccessful(response: AtlasFlyHttpResponse): T {
        ensureSuccessful(response)
        val body = response.body?.takeIf(String::isNotBlank)
            ?: throw kotlinx.serialization.SerializationException("Successful response body is empty")
        return json.decodeFromString(body)
    }

    private fun ensureSuccessful(response: AtlasFlyHttpResponse) {
        if (response.status !in 200..299) {
            throw RemoteHttpException(
                status = response.status,
                responseBody = response.body,
                retryAfterSeconds = response.headers[HttpHeaders.RetryAfter]?.toLongOrNull(),
            )
        }
    }

    private fun jsonBody(value: String) = TextContent(value, ContentType.Application.Json)

    private companion object {
        const val PROFILE = "profile"
        const val PREFERENCES = "profile-preferences"
        const val SAVED_PLACES = "saved-places"
        const val SAVED_TRIPS = "saved-trips"
        const val PUSH_TOKENS = "push-tokens"
        const val SUBSCRIPTION = "subscription"
        const val UPLOAD_PROFILE_IMAGE = "upload-profile-image"
    }
}
