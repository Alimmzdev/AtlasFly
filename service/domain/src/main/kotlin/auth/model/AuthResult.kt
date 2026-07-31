package auth.model

sealed interface AuthResult {
    data object Loading : AuthResult
    data object Success : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}