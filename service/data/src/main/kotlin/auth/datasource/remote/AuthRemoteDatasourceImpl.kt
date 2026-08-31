package auth.datasource.remote

import auth.model.AuthProvider
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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
        result.user?.sendEmailVerification(
            buildEmailActionCodeSettings(EMAIL_VERIFICATION_CONTINUE_URL)
        )?.await()
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

    override suspend fun resendEmailVerification() {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No signed-in Firebase user")
        user.sendEmailVerification(buildEmailActionCodeSettings(EMAIL_VERIFICATION_CONTINUE_URL)).await()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(
            email.trim(),
            buildEmailActionCodeSettings(PASSWORD_RESET_CONTINUE_URL),
        ).await()
    }

    override suspend fun verifyPasswordResetCode(oobCode: String): String {
        return firebaseAuth.verifyPasswordResetCode(oobCode).await()
    }

    override suspend fun confirmPasswordReset(oobCode: String, newPassword: String) {
        firebaseAuth.confirmPasswordReset(oobCode, newPassword).await()
    }

    private fun buildEmailActionCodeSettings(continueUrl: String): ActionCodeSettings {
        return ActionCodeSettings.newBuilder()
            .setUrl(continueUrl)
            .setHandleCodeInApp(true)
            .setAndroidPackageName(ANDROID_PACKAGE_NAME, false, null)
            .build()
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
        const val EMAIL_VERIFICATION_CONTINUE_URL: String =
            "https://atlasfly.nullexdev.tech/atlasfly-email-verified"
        const val PASSWORD_RESET_CONTINUE_URL: String =
            "https://atlasfly.nullexdev.tech/atlasfly-password-reset"
        const val ANDROID_PACKAGE_NAME: String = "dev.alimmz.atlasfly"
    }
}