package auth.datasource.remote

import auth.model.AuthProvider
import dev.alimmz.atlasfly.core.local.model.AuthTokens
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Github as SupabaseGithub
import io.github.jan.supabase.auth.providers.Google as SupabaseGoogle
import io.github.jan.supabase.auth.providers.builtin.Email
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AuthRemoteDatasourceImpl @Inject constructor(
    private val supabase: SupabaseClient,
) : AuthRemoteDatasource {

    override suspend fun isAuthorized(): Boolean {
        supabase.auth.awaitInitialization()
        if (supabase.auth.currentSessionOrNull() == null) return false
        val user = supabase.auth.retrieveUserForCurrentSession(updateSession = true)
        return user.emailConfirmedAt != null
    }

    override suspend fun login(provider: AuthProvider) {
        when (provider) {
            is AuthProvider.EmailPassword -> supabase.auth.signInWith(Email) {
                email = provider.email.trim()
                password = provider.password
            }
            AuthProvider.Google -> supabase.auth.signInWith(
                provider = SupabaseGoogle,
                redirectUrl = AUTH_REDIRECT_URL,
            )
            AuthProvider.Github -> supabase.auth.signInWith(
                provider = SupabaseGithub,
                redirectUrl = AUTH_REDIRECT_URL,
            ) {
                scopes.add("read:user")
                scopes.add("user:email")
            }
        }
    }

    override suspend fun signup(provider: AuthProvider.EmailPassword) {
        supabase.auth.signUpWith(
            provider = Email,
            redirectUrl = AUTH_REDIRECT_URL,
        ) {
            email = provider.email.trim()
            password = provider.password
        }
    }

    override suspend fun isEmailVerified(): Boolean {
        val user = supabase.auth.currentUserOrNull() ?: return false
        return user.emailConfirmedAt != null
    }

    override suspend fun getUnverifiedUserEmail(): String? {
        val user = supabase.auth.currentUserOrNull() ?: return null
        return user.email.takeIf { user.emailConfirmedAt == null }
    }

    override suspend fun getCurrentSession(): AuthTokens? {
        val session = supabase.auth.currentSessionOrNull() ?: return null
        val user = session.user ?: return null
        return AuthTokens(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            expiresAt = session.expiresAt.toEpochMilliseconds(),
            uid = user.id,
            email = user.email.orEmpty(),
            emailVerified = user.emailConfirmedAt != null,
        )
    }

    override suspend fun resendEmailVerification(email: String) {
        supabase.auth.resendEmail(
            type = OtpType.Email.SIGNUP,
            email = email.trim(),
            redirectUrl = AUTH_REDIRECT_URL,
        )
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        supabase.auth.resetPasswordForEmail(
            email = email.trim(),
            redirectUrl = AUTH_REDIRECT_URL,
        )
    }

    override suspend fun verifyPasswordRecoverySession(): String {
        val user = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException("No active password recovery session")
        return user.email ?: throw IllegalStateException("Recovery session has no email")
    }

    override suspend fun updatePassword(newPassword: String) {
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    override suspend fun refreshTokens() {
        supabase.auth.refreshCurrentSession()
    }

    override suspend fun logout() {
        supabase.auth.signOut()
    }

    private companion object {
        const val AUTH_REDIRECT_URL = "atlasfly://auth/callback"
    }
}
