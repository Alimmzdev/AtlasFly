package auth.datasource.local

import androidx.datastore.core.DataStore
import dev.alimmz.atlasfly.core.local.model.AuthSessionMetadata
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthLocalDatasourceImpl @Inject constructor(
    private val authSessionMetadataDataStore: DataStore<AuthSessionMetadata>,
) : AuthLocalDatasource {

    override suspend fun getSessionMetadata(): AuthSessionMetadata {
        return authSessionMetadataDataStore.data.first()
    }

    override suspend fun saveSessionMetadata(metadata: AuthSessionMetadata) {
        authSessionMetadataDataStore.updateData { metadata }
    }

    override suspend fun clearSessionMetadata() {
        authSessionMetadataDataStore.updateData { AuthSessionMetadata() }
    }
}
