package auth.token

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dev.alimmz.atlasfly.core.network.AuthTokenException
import dev.alimmz.atlasfly.core.network.AuthTokenProvider
import dev.alimmz.atlasfly.core.network.AuthenticatedToken
import dev.alimmz.atlasfly.service.data.BuildConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class FirebaseAuthTokenProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthTokenProvider {

    override suspend fun getToken(forceRefresh: Boolean): AuthenticatedToken {
        val user = firebaseAuth.currentUser ?: throw AuthTokenException.MissingUser()
        val token = try {
            user.getIdToken(forceRefresh).await().also { result ->
                if (BuildConfig.DEBUG) {
                    // Diagnostic fields only: never log the JWT, user ID, or full claims map.
                    val fields = listOf("iss", "aud", "role", "iat", "exp")
                    Log.d(
                        "FirebaseTokenCheck",
                        "forceRefresh=$forceRefresh " + fields.joinToString(" ") { field ->
                            "$field=${result.claims[field]}"
                        },
                    )
                }
            }.token
        } catch (error: CancellationException) {
            throw error
        } catch (_: FirebaseAuthInvalidUserException) {
            throw AuthTokenException.MissingUser()
        } catch (error: FirebaseNetworkException) {
            throw AuthTokenException.Network(error)
        } catch (error: Exception) {
            throw AuthTokenException.Unknown(error)
        }

        if (firebaseAuth.currentUser?.uid != user.uid) {
            throw AuthTokenException.UserChanged()
        }
        if (token.isNullOrBlank()) {
            throw AuthTokenException.MissingToken()
        }
        return AuthenticatedToken(value = token, userId = user.uid)
    }
}
