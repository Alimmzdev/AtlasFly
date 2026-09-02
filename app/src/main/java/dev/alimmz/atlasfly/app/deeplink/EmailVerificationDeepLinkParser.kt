package dev.alimmz.atlasfly.app.deeplink

import android.net.Uri

sealed interface EmailVerificationDeepLink {
    data class ActionCode(val oobCode: String) : EmailVerificationDeepLink
    data object VerifiedLanding : EmailVerificationDeepLink
}

object EmailVerificationDeepLinkParser {

    private const val HOST: String = "atlasfly.nullexdev.tech"
    private const val AUTH_ACTION_PATH: String = "/__/auth/action"
    private const val VERIFIED_LANDING_PATH: String = "/atlasfly-email-verified"
    private const val MODE_VERIFY_EMAIL: String = "verifyEmail"

    fun parse(uri: Uri?): EmailVerificationDeepLink? {
        if (uri == null || uri.host != HOST) {
            return null
        }
        val path: String = uri.path ?: return null
        return when {
            path.startsWith(AUTH_ACTION_PATH) -> parseAuthAction(uri)
            path.startsWith(VERIFIED_LANDING_PATH) -> EmailVerificationDeepLink.VerifiedLanding
            else -> null
        }
    }

    private fun parseAuthAction(uri: Uri): EmailVerificationDeepLink? {
        val mode: String = uri.getQueryParameter("mode") ?: return null
        if (mode != MODE_VERIFY_EMAIL) {
            return null
        }
        val oobCode: String = uri.getQueryParameter("oobCode") ?: return null
        return EmailVerificationDeepLink.ActionCode(oobCode)
    }
}
