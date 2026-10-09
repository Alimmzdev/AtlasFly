package dev.alimmz.atlasfly.app.deeplink

import android.net.Uri

sealed interface AuthDeepLink {
    data object PasswordRecovery : AuthDeepLink
    data object SessionCallback : AuthDeepLink
}

object AuthDeepLinkParser {

    private const val AUTH_SCHEME: String = "atlasfly"
    private const val AUTH_HOST: String = "auth"

    fun parse(uri: Uri?): AuthDeepLink? {
        if (uri == null) return null
        val isCustomScheme = uri.scheme == AUTH_SCHEME && uri.host == AUTH_HOST
        val isAppHost = (uri.scheme == "https" || uri.scheme == "http") &&
            (uri.host == "atlasfly.nullexdev.tech" || uri.host == "localhost" || uri.host == "192.168.1.68")
        if (!isCustomScheme && !isAppHost) return null

        val type = authCallbackParameter(uri, "type")
        val path = uri.path.orEmpty()
        return if (type == "recovery" || path.contains("reset-password")) {
            AuthDeepLink.PasswordRecovery
        } else {
            AuthDeepLink.SessionCallback
        }
    }

    fun extractToken(uri: Uri?): String? {
        if (uri == null) return null
        return authCallbackParameter(uri, "token")
            ?: authCallbackParameter(uri, "code")
    }

    fun extractEmail(uri: Uri?): String? {
        if (uri == null) return null
        return authCallbackParameter(uri, "email")
    }

    fun extractType(uri: Uri?): String? {
        if (uri == null) return null
        return authCallbackParameter(uri, "type")
    }

    private fun authCallbackParameter(uri: Uri, name: String): String? {
        uri.getQueryParameter(name)?.let { return it }
        val fragment = uri.fragment ?: return null
        return Uri.parse("$AUTH_SCHEME://$AUTH_HOST?$fragment").getQueryParameter(name)
    }
}
