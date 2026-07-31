package auth.datasource.remote

import auth.model.AuthProvider
import tech.nullexdev.atlasfly.core.local.model.AuthTokens

interface AuthRemoteDatasource {
    suspend fun isAuthorized(): Boolean
    suspend fun login(provider: AuthProvider)
    suspend fun refreshTokens()
    suspend fun logout()
}