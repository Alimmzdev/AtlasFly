package auth.model

sealed interface AuthProvider {
    data class EmailPassword(val email: String, val password: String) : AuthProvider
    data object Google : AuthProvider
    data object Github : AuthProvider
}
