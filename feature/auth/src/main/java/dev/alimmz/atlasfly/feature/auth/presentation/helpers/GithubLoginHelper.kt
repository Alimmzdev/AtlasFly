package dev.alimmz.atlasfly.feature.auth.presentation.helpers

import android.app.Activity
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider

internal fun launchGitHubLogin(
    activity: Activity,
    onSuccess: (user: FirebaseUser, githubAccessToken: String?) -> Unit,
    onFailure: (Exception) -> Unit,
) {
    val provider = OAuthProvider.newBuilder("github.com")
    provider.scopes = listOf("read:user")

    val auth = FirebaseAuth.getInstance()
    val pending = auth.pendingAuthResult

    fun handleResult(result: AuthResult) {
        val user = result.user
        if (user == null) {
            onFailure(IllegalStateException("Sign-in succeeded but user is null"))
            return
        }
        val credential = result.credential as? OAuthCredential
        val githubAccessToken = credential?.accessToken
        onSuccess(user, githubAccessToken)
    }

    if (pending != null) {
        // There's already a pending sign-in, continue it
        pending
            .addOnSuccessListener { result -> handleResult(result) }
            .addOnFailureListener { e -> onFailure(e) }
    } else {
        auth.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener { result -> handleResult(result) }
            .addOnFailureListener { e -> onFailure(e) }
    }
}