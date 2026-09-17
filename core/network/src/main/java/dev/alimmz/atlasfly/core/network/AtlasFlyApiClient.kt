package dev.alimmz.atlasfly.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.path
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class AtlasFlyApiClient @Inject constructor(
    @AtlasFlyHttpClient private val httpClient: HttpClient,
    private val authTokenProvider: AuthTokenProvider,
    @SupabasePublishableKey private val publishableKey: String,
) {
    private val refreshMutex = Mutex()

    suspend fun execute(
        method: HttpMethod,
        pathSegments: List<String>,
        parameters: Parameters = Parameters.Empty,
        bodyFactory: (() -> OutgoingContent)? = null,
        canRetryAfterUnauthorized: Boolean = true,
    ): AtlasFlyHttpResponse {
        if (publishableKey.isBlank()) {
            throw NetworkClientException.Configuration("SUPABASE_PUBLISHABLE_KEY is missing")
        }

        val originalToken = authTokenProvider.getToken()
        val firstResponse = send(
            method = method,
            pathSegments = pathSegments,
            parameters = parameters,
            token = originalToken,
            bodyFactory = bodyFactory,
        )
        if (firstResponse.status != HttpStatusCode.Unauthorized.value || !canRetryAfterUnauthorized) {
            return firstResponse
        }

        val refreshedToken = refreshRejectedToken(originalToken)
        return send(
            method = method,
            pathSegments = pathSegments,
            parameters = parameters,
            token = refreshedToken,
            bodyFactory = bodyFactory,
        )
    }

    private suspend fun refreshRejectedToken(rejectedToken: AuthenticatedToken): AuthenticatedToken =
        refreshMutex.withLock {
            val currentToken = authTokenProvider.getToken()
            if (currentToken.userId != rejectedToken.userId) {
                throw AuthTokenException.UserChanged()
            }
            if (currentToken.value != rejectedToken.value) {
                return@withLock currentToken
            }

            val refreshedToken = authTokenProvider.getToken(forceRefresh = true)
            if (refreshedToken.userId != rejectedToken.userId) {
                throw AuthTokenException.UserChanged()
            }
            refreshedToken
        }

    private suspend fun send(
        method: HttpMethod,
        pathSegments: List<String>,
        parameters: Parameters,
        token: AuthenticatedToken,
        bodyFactory: (() -> OutgoingContent)?,
    ): AtlasFlyHttpResponse {
        return try {
            val response = httpClient.request {
                this.method = method
                url {
                    protocol = URLProtocol.HTTPS
                    host = TRUSTED_HOST
                    path("functions", "v1")
                    appendPathSegments(pathSegments)
                    this.parameters.appendAll(parameters)
                }
                header(HttpHeaders.Authorization, "Bearer ${token.value}")
                header(API_KEY_HEADER, publishableKey)
                bodyFactory?.let { setBody(it()) }
            }
            AtlasFlyHttpResponse(
                status = response.status.value,
                body = if (response.status == HttpStatusCode.NoContent) null else response.bodyAsText(),
                headers = response.headers,
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            throw NetworkClientException.Connectivity(error)
        }
    }

    private companion object {
        const val TRUSTED_HOST = "tstjkjlbdxnsqhjtzvau.supabase.co"
        const val API_KEY_HEADER = "apikey"
    }
}

data class AtlasFlyHttpResponse(
    val status: Int,
    val body: String?,
    val headers: Headers,
)

sealed class NetworkClientException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Configuration(message: String) : NetworkClientException(message)
    class Connectivity(cause: Throwable) : NetworkClientException("Network request failed", cause)
}
