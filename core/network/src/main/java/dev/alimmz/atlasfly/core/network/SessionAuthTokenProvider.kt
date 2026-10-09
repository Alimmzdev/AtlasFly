package dev.alimmz.atlasfly.core.network

import androidx.datastore.core.DataStore
import dev.alimmz.atlasfly.core.local.model.AuthSessionMetadata
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionAuthTokenProvider @Inject constructor(
    private val dataStore: DataStore<AuthSessionMetadata>,
) : AuthTokenProvider {

    override suspend fun getToken(forceRefresh: Boolean): AuthenticatedToken {
        val session = dataStore.data.first()
        if (session.accessToken.isBlank()) {
            throw AuthTokenException.MissingToken()
        }
        if (session.uid.isBlank()) {
            throw AuthTokenException.MissingUser()
        }
        if (forceRefresh) {
            throw AuthTokenException.MissingUser()
        }
        return AuthenticatedToken(
            value = session.accessToken,
            userId = session.uid,
        )
    }
}
