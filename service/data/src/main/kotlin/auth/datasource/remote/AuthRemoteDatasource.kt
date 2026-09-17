package auth.datasource.remote

import auth.model.AuthProvider
import dev.alimmz.atlasfly.core.local.model.AuthSessionMetadata

interface AuthRemoteDatasource {
    suspend fun isAuthorized(): Boolean
    suspend fun login(provider: AuthProvider)
    suspend fun signup(provider: AuthProvider.EmailPassword)
    suspend fun isEmailVerified(): Boolean
    suspend fun getUnverifiedUserEmail(): String?
    suspend fun getCurrentSession(): AuthSessionMetadata?
    suspend fun resendEmailVerification(email: String)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun verifyPasswordRecoverySession(): String
    suspend fun updatePassword(newPassword: String)
    suspend fun refreshTokens()
    suspend fun logout()
}
