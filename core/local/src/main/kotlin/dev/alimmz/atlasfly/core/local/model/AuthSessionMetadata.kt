package dev.alimmz.atlasfly.core.local.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthSessionMetadata(
    val accessToken: String = "",
    val tokenType: String = "Bearer",
    val uid: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
    val expiresAt: String = "",
) {
    val hasVerifiedSession: Boolean
        get() = emailVerified && uid.isNotEmpty() && accessToken.isNotEmpty()
}
