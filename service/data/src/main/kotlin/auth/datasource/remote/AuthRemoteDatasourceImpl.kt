package auth.datasource.remote

import android.util.Log
import auth.model.AuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import dev.alimmz.atlasfly.core.local.model.AuthSessionMetadata
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class AuthRemoteDatasourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthRemoteDatasource {

    override suspend fun isAuthorized(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        return try {
            user.reload().await()
            user.isEmailVerified
        } catch (_: FirebaseAuthInvalidUserException) {
            firebaseAuth.signOut()
            false
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            user.isEmailVerified
        }
    }

    override suspend fun login(provider: AuthProvider) {
        when (provider) {
            is AuthProvider.EmailPassword -> {
                firebaseAuth.signInWithEmailAndPassword(
                    provider.email,
                    provider.password,
                ).await()
            }

            is AuthProvider.Google -> {
                val credential = GoogleAuthProvider.getCredential(provider.idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
            }

            is AuthProvider.Github -> {
                val credential = GithubAuthProvider.getCredential(provider.accessToken)
                firebaseAuth.signInWithCredential(credential).await()
            }
        }
    }

    override suspend fun signup(provider: AuthProvider.EmailPassword) {
        val result = firebaseAuth
            .createUserWithEmailAndPassword(
                provider.email,
                provider.password
            )
            .await()
        val user = result.user
            ?: firebaseAuth.currentUser
            ?: throw IllegalStateException("No Firebase user after signup")
        sendVerificationEmail(user)
    }

    override suspend fun verifyEmail(oobCode: String) {
        firebaseAuth.applyActionCode(oobCode).await()
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No signed-in Firebase user")
        user.reload().await()
        if (!user.isEmailVerified) {
            throw IllegalStateException("Email verification failed")
        }
    }

    override suspend fun isEmailVerified(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        user.reload().await()
        return user.isEmailVerified
    }

    override suspend fun getUnverifiedUserEmail(): String? {
        val user = firebaseAuth.currentUser ?: return null
        return try {
            user.reload().await()
            if (user.isEmailVerified) null else user.email
        } catch (_: FirebaseAuthInvalidUserException) {
            firebaseAuth.signOut()
            null
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            if (user.isEmailVerified) null else user.email
        }
    }

    override suspend fun getCurrentSession(): AuthSessionMetadata? {
        val user = firebaseAuth.currentUser ?: return null
        return AuthSessionMetadata(
            uid = user.uid,
            email = user.email.orEmpty(),
            emailVerified = user.isEmailVerified,
        )
    }

    override suspend fun resendEmailVerification() {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No signed-in Firebase user")
        sendVerificationEmail(user)
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
        } catch (e: Exception) {
            logFirebaseFailure("sendPasswordResetEmail", e)
            throw e
        }
    }

    override suspend fun verifyPasswordResetCode(oobCode: String): String {
        return firebaseAuth.verifyPasswordResetCode(oobCode).await()
    }

    override suspend fun confirmPasswordReset(oobCode: String, newPassword: String) {
        firebaseAuth.confirmPasswordReset(oobCode, newPassword).await()
    }

    private suspend fun sendVerificationEmail(user: FirebaseUser) {
        try {
            user.sendEmailVerification().await()
        } catch (e: Exception) {
            Log.e(TAG, "Verification email failed", e)
            throw e
        }
    }

    private fun logFirebaseFailure(action: String, error: Exception) {
        val authError = error as? FirebaseAuthException
        Log.e(
            TAG,
            "$action failed code=${authError?.errorCode} message=${error.message}",
            error,
        )
    }

    override suspend fun refreshTokens() {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No signed-in Firebase user")
        user.getIdToken(true).await()
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    private companion object {
        const val TAG: String = "AtlasFlyAuth"
    }
}
