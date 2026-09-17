package dev.alimmz.atlasfly.core.network

interface AuthTokenProvider {
    suspend fun getToken(forceRefresh: Boolean = false): AuthenticatedToken
}

class AuthenticatedToken(
    val value: String,
    val userId: String,
) {
    override fun toString(): String = "AuthenticatedToken([REDACTED])"
}

sealed class AuthTokenException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class MissingUser : AuthTokenException("No Supabase user is signed in")
    class MissingToken : AuthTokenException("Supabase returned an empty access token")
    class UserChanged : AuthTokenException("The Supabase user changed while retrieving a token")
    class Network(cause: Throwable) : AuthTokenException("Supabase token retrieval failed", cause)
    class Unknown(cause: Throwable) : AuthTokenException("Supabase token retrieval failed", cause)
}
