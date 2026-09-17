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
    class MissingUser : AuthTokenException("No Firebase user is signed in")
    class MissingToken : AuthTokenException("Firebase returned an empty ID token")
    class UserChanged : AuthTokenException("The Firebase user changed while retrieving a token")
    class Network(cause: Throwable) : AuthTokenException("Firebase token retrieval failed", cause)
    class Unknown(cause: Throwable) : AuthTokenException("Firebase token retrieval failed", cause)
}
