package auth.repository

import auth.model.AuthProvider
import auth.model.AuthResult
import auth.model.ResetCodeResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun isAuthorized(): Boolean
    fun login(provider: AuthProvider): Flow<AuthResult>
    fun signup(provider: AuthProvider.EmailPassword): Flow<AuthResult>
    fun verifyEmail(oobCode: String): Flow<AuthResult>
    suspend fun isEmailVerified(): Boolean
    fun resendEmailVerification(): Flow<AuthResult>
    fun sendPasswordResetEmail(email: String): Flow<AuthResult>
    suspend fun verifyPasswordResetCode(oobCode: String): ResetCodeResult
    fun confirmPasswordReset(oobCode: String, newPassword: String): Flow<AuthResult>
    fun refreshTokens(): Flow<AuthResult>
    suspend fun logout()
}