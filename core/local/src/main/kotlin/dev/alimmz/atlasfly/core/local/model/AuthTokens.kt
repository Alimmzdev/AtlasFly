package dev.alimmz.atlasfly.core.local.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val accessToken: String = "",
    val refreshToken: String = "",
    val expiresAt: Long = 0L,
    val uid: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
) {
    val hasVerifiedSession: Boolean
        get() = emailVerified && uid.isNotEmpty()
}
