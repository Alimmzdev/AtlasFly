package dev.alimmz.atlasfly.app.deeplink

import android.net.Uri

sealed interface AuthDeepLink {
    data object PasswordRecovery : AuthDeepLink
    data object SessionCallback : AuthDeepLink
}

object AuthDeepLinkParser {

    private const val SUPABASE_SCHEME: String = "atlasfly"
    private const val SUPABASE_HOST: String = "auth"

    fun parse(uri: Uri?): AuthDeepLink? {
        if (uri?.scheme != SUPABASE_SCHEME || uri.host != SUPABASE_HOST) return null
        return if (authCallbackParameter(uri, "type") == "recovery") {
            AuthDeepLink.PasswordRecovery
        } else {
            AuthDeepLink.SessionCallback
        }
    }

    private fun authCallbackParameter(uri: Uri, name: String): String? {
        uri.getQueryParameter(name)?.let { return it }
        val fragment = uri.fragment ?: return null
        return Uri.parse("$SUPABASE_SCHEME://$SUPABASE_HOST?$fragment").getQueryParameter(name)
    }
}
