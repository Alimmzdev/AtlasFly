package auth.repository

import auth.model.AuthProvider
import auth.model.AuthResult
import auth.model.ResetCodeResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun isAuthorized(): Boolean
    fun login(provider: AuthProvider): Flow<AuthResult>
    fun signup(provider: AuthProvider.EmailPassword): Flow<AuthResult>
    suspend fun isEmailVerified(): Boolean
    suspend fun getUnverifiedUserEmail(): String?
    fun resendEmailVerification(email: String): Flow<AuthResult>
    fun sendPasswordResetEmail(email: String): Flow<AuthResult>
    suspend fun verifyPasswordRecoverySession(): ResetCodeResult
    fun updatePassword(newPassword: String): Flow<AuthResult>
    fun refreshTokens(): Flow<AuthResult>
    suspend fun logout()
}
