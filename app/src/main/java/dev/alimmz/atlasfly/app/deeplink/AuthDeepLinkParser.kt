package dev.alimmz.atlasfly.app.deeplink

import android.net.Uri

sealed interface AuthDeepLink {
    data class VerifyEmail(val oobCode: String) : AuthDeepLink
    data object EmailVerifiedLanding : AuthDeepLink
    data class ResetPassword(val oobCode: String) : AuthDeepLink
    data object PasswordResetLanding : AuthDeepLink
}

object AuthDeepLinkParser {

    private const val HOST: String = "atlasfly.nullexdev.tech"
    private const val AUTH_ACTION_PATH: String = "/__/auth/action"
    private const val VERIFIED_LANDING_PATH: String = "/atlasfly-email-verified"
    private const val PASSWORD_RESET_LANDING_PATH: String = "/atlasfly-password-reset"
    private const val MODE_VERIFY_EMAIL: String = "verifyEmail"
    private const val MODE_RESET_PASSWORD: String = "resetPassword"

    fun parse(uri: Uri?): AuthDeepLink? {
        if (uri == null || uri.host != HOST) {
            return null
        }
        val path: String = uri.path ?: return null
        return when {
            path.startsWith(AUTH_ACTION_PATH) -> parseAuthAction(uri)
            path.startsWith(VERIFIED_LANDING_PATH) -> AuthDeepLink.EmailVerifiedLanding
            path.startsWith(PASSWORD_RESET_LANDING_PATH) -> AuthDeepLink.PasswordResetLanding
            else -> null
        }
    }

    private fun parseAuthAction(uri: Uri): AuthDeepLink? {
        val mode: String = uri.getQueryParameter("mode") ?: return null
        val oobCode: String = uri.getQueryParameter("oobCode") ?: return null
        return when (mode) {
            MODE_VERIFY_EMAIL -> AuthDeepLink.VerifyEmail(oobCode)
            MODE_RESET_PASSWORD -> AuthDeepLink.ResetPassword(oobCode)
            else -> null
        }
    }
}
