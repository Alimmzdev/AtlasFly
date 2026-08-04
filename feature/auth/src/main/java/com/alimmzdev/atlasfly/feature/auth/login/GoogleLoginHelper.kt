package com.alimmzdev.atlasfly.feature.auth.login

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

internal suspend fun launchGoogleLogin(
    activity: Activity,
    serverClientId: String,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
) {
    try {
        val credentialManager = CredentialManager.create(activity)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(serverClientId)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            val googleCredential =
                GoogleIdTokenCredential.createFrom(
                    credential.data
                )

            onSuccess(
                googleCredential.idToken
            )
        }

    } catch (e: Exception) {
        onError(e)
    }
}