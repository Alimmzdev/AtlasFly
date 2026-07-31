package auth.model

sealed interface AuthProvider {
    data class EmailPassword(val email: String, val password: String) : AuthProvider
    data class Google(val idToken: String) : AuthProvider
    data class Apple(val idToken: String, val nonce: String? = null) : AuthProvider
}
