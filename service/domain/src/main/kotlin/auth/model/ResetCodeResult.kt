package auth.model

sealed interface ResetCodeResult {
    data class Valid(val email: String) : ResetCodeResult
    data class Invalid(val error: AuthError) : ResetCodeResult
}
