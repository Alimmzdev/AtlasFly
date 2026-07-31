package auth.datasource.local

import kotlinx.coroutines.flow.Flow
import tech.nullexdev.atlasfly.core.local.model.AuthTokens

interface AuthLocalDatasource {
    suspend fun isAuthorized(): Boolean
    suspend fun getAuthTokens(): AuthTokens
    suspend fun saveAuthTokens(authTokens: AuthTokens)
}