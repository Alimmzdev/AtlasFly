package auth.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class EmailRequest(
    val email: String,
)

@Serializable
data class VerifyEmailRequest(
    val token: String,
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
)

@Serializable
data class MessageResponse(
    val message: String = "",
)

@Serializable
data class AccountView(
    val id: String,
    val email: String,
    val emailVerified: Boolean,
    val createdAt: String? = null,
)

@Serializable
data class AuthenticationSession(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long? = null,
    val expiresAt: String? = null,
    val user: AccountView,
)

@Serializable
data class OAuthStartResponse(
    val authorizationUrl: String,
    val expiresAt: String? = null,
)

@Serializable
data class ProblemDetails(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val errors: Map<String, String>? = null,
)

class AuthApiException(
    val statusCode: Int,
    val problem: ProblemDetails? = null,
    override val message: String? = problem?.detail ?: problem?.title ?: "Authentication error: $statusCode",
) : Exception(message)
