package auth.datasource.local

import androidx.datastore.core.DataStore
import dev.alimmz.atlasfly.core.local.model.AuthSessionMetadata
import javax.inject.Inject

class AuthLocalDatasourceImpl @Inject constructor(
    private val authSessionMetadataDataStore: DataStore<AuthSessionMetadata>,
) : AuthLocalDatasource {

    override suspend fun saveSessionMetadata(metadata: AuthSessionMetadata) {
        authSessionMetadataDataStore.updateData { metadata }
    }

    override suspend fun clearSessionMetadata() {
        authSessionMetadataDataStore.updateData { AuthSessionMetadata() }
    }
}
