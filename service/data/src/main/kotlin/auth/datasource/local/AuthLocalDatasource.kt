package auth.datasource.local

import dev.alimmz.atlasfly.core.local.model.AuthTokens

interface AuthLocalDatasource {
    suspend fun isAuthorized(): Boolean
    suspend fun getAuthTokens(): AuthTokens
    suspend fun saveAuthTokens(authTokens: AuthTokens)
    suspend fun clearAuthTokens()
}
