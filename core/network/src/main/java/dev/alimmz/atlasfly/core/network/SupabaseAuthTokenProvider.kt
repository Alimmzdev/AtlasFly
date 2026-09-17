package dev.alimmz.atlasfly.core.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.HttpRequestException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class SupabaseAuthTokenProvider @Inject constructor(
    private val supabase: SupabaseClient,
) : AuthTokenProvider {

    override suspend fun getToken(forceRefresh: Boolean): AuthenticatedToken {
        return try {
            supabase.auth.awaitInitialization()

            val initialSession = supabase.auth.currentSessionOrNull()
                ?: throw AuthTokenException.MissingUser()
            val initialUserId = initialSession.user?.id
                ?: throw AuthTokenException.MissingUser()

            if (forceRefresh) {
                supabase.auth.refreshCurrentSession()
            }

            val session = supabase.auth.currentSessionOrNull()
                ?: throw AuthTokenException.MissingUser()
            val userId = session.user?.id
                ?: throw AuthTokenException.MissingUser()
            if (userId != initialUserId) {
                throw AuthTokenException.UserChanged()
            }
            if (session.accessToken.isBlank()) {
                throw AuthTokenException.MissingToken()
            }

            AuthenticatedToken(
                value = session.accessToken,
                userId = userId,
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: AuthTokenException) {
            throw error
        } catch (error: HttpRequestException) {
            throw AuthTokenException.Network(error)
        } catch (error: Exception) {
            throw AuthTokenException.Unknown(error)
        }
    }
}
