package auth.datasource.remote

import auth.model.AuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDatasourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthRemoteDatasource {

    override suspend fun isAuthorized(): Boolean {
        return firebaseAuth.currentUser != null
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

    override suspend fun refreshTokens() {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No signed-in Firebase user")
        user.getIdToken(true).await()
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }
}