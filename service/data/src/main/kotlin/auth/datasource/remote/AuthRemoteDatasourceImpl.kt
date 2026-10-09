package auth.datasource.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import auth.datasource.local.AuthLocalDatasource
import auth.model.AccountView
import auth.model.AuthApiException
import auth.model.AuthProvider
import auth.model.AuthenticationSession
import auth.model.EmailRequest
import auth.model.LoginRequest
import auth.model.MessageResponse
import auth.model.ProblemDetails
import auth.model.RegisterRequest
import auth.model.ResetPasswordRequest
import auth.model.VerifyEmailRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.alimmz.atlasfly.core.local.model.AuthSessionMetadata
import dev.alimmz.atlasfly.core.network.AtlasFlyHttpClient
import dev.alimmz.atlasfly.core.network.AuthBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

@Singleton
class AuthRemoteDatasourceImpl @Inject constructor(
    @AtlasFlyHttpClient private val httpClient: HttpClient,
    private val json: Json,
    @AuthBaseUrl private val authBaseUrl: String,
    @ApplicationContext private val context: Context,
    private val authLocalDatasource: AuthLocalDatasource,
) : AuthRemoteDatasource {

    private var currentSession: AuthSessionMetadata? = null
    private var lastUnverifiedEmail: String? = null
    private var pendingSignupPassword: String? = null
    private var pendingRecoveryToken: String? = null
    private var pendingRecoveryEmail: String? = null
    private var lastPasswordResetEmail: String? = null

    override suspend fun isAuthorized(): Boolean {
        val localSession = authLocalDatasource.getSessionMetadata()
        val token = localSession.accessToken.takeIf { it.isNotBlank() }
            ?: currentSession?.accessToken?.takeIf { it.isNotBlank() }
            ?: return false

        val response = httpClient.get("$authBaseUrl/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.NotFound) {
            currentSession = null
            authLocalDatasource.clearSessionMetadata()
            return false
        }

        if (response.status.value !in 200..299) {
            val problem = parseProblemDetails(response)
            throw AuthApiException(response.status.value, problem)
        }

        val account = json.decodeFromString<AccountView>(response.bodyAsText())
        val updatedSession = localSession.copy(
            uid = account.id,
            email = account.email,
            emailVerified = account.emailVerified,
        )
        currentSession = updatedSession
        authLocalDatasource.saveSessionMetadata(updatedSession)
        return account.emailVerified
    }

    override suspend fun login(provider: AuthProvider) {
        when (provider) {
            is AuthProvider.EmailPassword -> {
                val request = LoginRequest(
                    email = provider.email.trim(),
                    password = provider.password,
                )
                val response = httpClient.post("$authBaseUrl/api/v1/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(LoginRequest.serializer(), request))
                }

                if (response.status == HttpStatusCode.Forbidden) {
                    lastUnverifiedEmail = provider.email.trim()
                    val problem = parseProblemDetails(response)
                    throw AuthApiException(403, problem, problem?.detail ?: "Email not verified")
                }

                val session = handleResponse<AuthenticationSession>(response)
                val metadata = AuthSessionMetadata(
                    accessToken = session.accessToken,
                    tokenType = session.tokenType,
                    uid = session.user.id,
                    email = session.user.email,
                    emailVerified = session.user.emailVerified,
                    expiresAt = session.expiresAt.orEmpty(),
                )
                currentSession = metadata
                lastUnverifiedEmail = null
                pendingSignupPassword = null
                authLocalDatasource.saveSessionMetadata(metadata)
            }

            AuthProvider.Google -> {
                launchOAuth("google")
            }

            AuthProvider.Github -> {
                launchOAuth("github")
            }
        }
    }

    override suspend fun signup(provider: AuthProvider.EmailPassword) {
        val request = RegisterRequest(
            email = provider.email.trim(),
            password = provider.password,
        )
        val response = httpClient.post("$authBaseUrl/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(RegisterRequest.serializer(), request))
        }
        handleResponse<MessageResponse>(response)
        lastUnverifiedEmail = provider.email.trim()
        pendingSignupPassword = provider.password
    }

    override suspend fun isEmailVerified(): Boolean {
        val localSession = authLocalDatasource.getSessionMetadata()
        val token = localSession.accessToken.takeIf { it.isNotBlank() }
            ?: currentSession?.accessToken?.takeIf { it.isNotBlank() }

        if (!token.isNullOrBlank()) {
            return try {
                val response = httpClient.get("$authBaseUrl/api/v1/auth/me") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
                if (response.status.value in 200..299) {
                    val account = json.decodeFromString<AccountView>(response.bodyAsText())
                    val updated = localSession.copy(
                        uid = account.id,
                        email = account.email,
                        emailVerified = account.emailVerified,
                    )
                    currentSession = updated
                    authLocalDatasource.saveSessionMetadata(updated)
                    account.emailVerified
                } else {
                    false
                }
            } catch (_: Exception) {
                localSession.emailVerified
            }
        }

        val email = lastUnverifiedEmail ?: localSession.email.takeIf { it.isNotBlank() }
        val password = pendingSignupPassword
        if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            return try {
                login(AuthProvider.EmailPassword(email, password))
                true
            } catch (e: AuthApiException) {
                if (e.statusCode == 403) {
                    false
                } else {
                    throw e
                }
            }
        }

        return false
    }

    override suspend fun getUnverifiedUserEmail(): String? {
        return lastUnverifiedEmail
            ?: authLocalDatasource.getSessionMetadata().takeIf { !it.emailVerified }?.email?.takeIf { it.isNotBlank() }
    }

    override suspend fun getCurrentSession(): AuthSessionMetadata? {
        return currentSession ?: authLocalDatasource.getSessionMetadata().takeIf { it.accessToken.isNotBlank() }
    }

    override suspend fun resendEmailVerification(email: String) {
        val request = EmailRequest(email = email.trim())
        val response = httpClient.post("$authBaseUrl/api/v1/auth/resend-verification") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(EmailRequest.serializer(), request))
        }
        handleResponse<MessageResponse>(response)
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        val request = EmailRequest(email = email.trim())
        val response = httpClient.post("$authBaseUrl/api/v1/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(EmailRequest.serializer(), request))
        }
        handleResponse<MessageResponse>(response)
        lastPasswordResetEmail = email.trim()
    }

    override suspend fun verifyPasswordRecoverySession(): String {
        pendingRecoveryToken ?: throw IllegalStateException("No active password recovery session")
        return pendingRecoveryEmail ?: lastPasswordResetEmail ?: ""
    }

    override suspend fun updatePassword(newPassword: String) {
        val token = pendingRecoveryToken ?: throw IllegalStateException("No active password recovery token")
        val request = ResetPasswordRequest(
            token = token,
            newPassword = newPassword,
        )
        val response = httpClient.post("$authBaseUrl/api/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(ResetPasswordRequest.serializer(), request))
        }
        handleResponse<MessageResponse>(response)
        pendingRecoveryToken = null
        pendingRecoveryEmail = null
        currentSession = null
        authLocalDatasource.clearSessionMetadata()
    }

    override suspend fun refreshTokens() {
        val localSession = authLocalDatasource.getSessionMetadata()
        val token = localSession.accessToken.takeIf { it.isNotBlank() }
            ?: currentSession?.accessToken?.takeIf { it.isNotBlank() }
            ?: throw AuthApiException(401, null, "No active session")

        val response = httpClient.get("$authBaseUrl/api/v1/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status.value !in 200..299) {
            val problem = parseProblemDetails(response)
            throw AuthApiException(response.status.value, problem)
        }
    }

    override suspend fun logout() {
        try {
            val localSession = authLocalDatasource.getSessionMetadata()
            val token = localSession.accessToken.takeIf { it.isNotBlank() }
                ?: currentSession?.accessToken?.takeIf { it.isNotBlank() }
            if (!token.isNullOrBlank()) {
                httpClient.post("$authBaseUrl/api/v1/auth/logout") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        } catch (_: Exception) {
        } finally {
            currentSession = null
            pendingRecoveryToken = null
            pendingRecoveryEmail = null
            pendingSignupPassword = null
            lastUnverifiedEmail = null
            authLocalDatasource.clearSessionMetadata()
        }
    }

    override suspend fun verifyEmail(token: String) {
        val request = VerifyEmailRequest(token = token)
        val response = httpClient.post("$authBaseUrl/api/v1/auth/verify-email") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(VerifyEmailRequest.serializer(), request))
        }
        handleResponse<MessageResponse>(response)
    }

    override fun setPasswordResetToken(token: String, email: String?) {
        pendingRecoveryToken = token
        if (!email.isNullOrBlank()) {
            pendingRecoveryEmail = email
        }
    }

    private fun launchOAuth(provider: String) {
        val url = "$authBaseUrl/api/v1/auth/oauth/$provider"
        try {
            val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder().build()
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customTabsIntent.launchUrl(context, url.toUri())
        } catch (e: Exception) {
            // Fallback to system browser if Custom Tabs isn't available
            val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private suspend inline fun <reified T> handleResponse(response: HttpResponse): T {
        val status = response.status.value
        val body = response.bodyAsText()
        if (status in 200..299) {
            if (body.isBlank()) {
                if (T::class == MessageResponse::class) {
                    @Suppress("UNCHECKED_CAST")
                    return MessageResponse() as T
                }
            }
            return json.decodeFromString(body)
        }
        val problem = try {
            json.decodeFromString<ProblemDetails>(body)
        } catch (_: Exception) {
            ProblemDetails(status = status, detail = body.ifBlank { null })
        }
        throw AuthApiException(status, problem)
    }

    private suspend fun parseProblemDetails(response: HttpResponse): ProblemDetails? {
        return try {
            val body = response.bodyAsText()
            if (body.isNotBlank()) {
                json.decodeFromString<ProblemDetails>(body)
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
