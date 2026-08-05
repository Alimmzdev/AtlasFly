package auth.repository

import auth.model.AuthProvider
import auth.model.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun isAuthorized(): Boolean
    fun login(provider: AuthProvider): Flow<AuthResult>
    fun signup(provider: AuthProvider.EmailPassword): Flow<AuthResult>
    fun refreshTokens(): Flow<AuthResult>
    suspend fun logout()
}