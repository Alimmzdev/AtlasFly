package auth.datasource.local

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import dev.alimmz.atlasfly.core.local.model.AuthTokens
import javax.inject.Inject

class AuthLocalDatasourceImpl @Inject constructor(
    private val authTokensDataStore: DataStore<AuthTokens>,
) : AuthLocalDatasource {

    override suspend fun isAuthorized(): Boolean {
        return authTokensDataStore.data
            .map { it.accessToken.isNotEmpty() }
            .first()
    }

    override suspend fun getAuthTokens(): AuthTokens {
        return authTokensDataStore.data.first()
    }

    override suspend fun saveAuthTokens(authTokens: AuthTokens) {
        authTokensDataStore.updateData { authTokens }
    }
}