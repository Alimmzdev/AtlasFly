package auth.datasource.local

import dev.alimmz.atlasfly.core.local.model.AuthSessionMetadata

interface AuthLocalDatasource {
    suspend fun getSessionMetadata(): AuthSessionMetadata
    suspend fun saveSessionMetadata(metadata: AuthSessionMetadata)
    suspend fun clearSessionMetadata()
}
