package tech.nullexdev.atlasfly.core.local.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val accessToken: String = "",
    val refreshToken: String = "",
    val expiresAt: Long = 0L
)