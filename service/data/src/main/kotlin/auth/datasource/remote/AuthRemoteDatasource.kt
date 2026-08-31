package auth.datasource.remote

import auth.model.AuthProvider
import dev.alimmz.atlasfly.core.local.model.AuthTokens

interface AuthRemoteDatasource {
    suspend fun isAuthorized(): Boolean
    suspend fun login(provider: AuthProvider)
    suspend fun signup(provider: AuthProvider.EmailPassword)
    suspend fun verifyEmail(oobCode: String)
    suspend fun isEmailVerified(): Boolean
    suspend fun resendEmailVerification()
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun verifyPasswordResetCode(oobCode: String): String
    suspend fun confirmPasswordReset(oobCode: String, newPassword: String)
    suspend fun refreshTokens()
    suspend fun logout()
}