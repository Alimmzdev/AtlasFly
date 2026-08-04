package auth.model

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object UserNotFound : AuthError
    data object AccountExistsDifferentProvider : AuthError
    data object NetworkError : AuthError
    data object TooManyAttempts : AuthError
    data object Cancelled : AuthError
    data class Unknown(val cause: Throwable? = null) : AuthError
}