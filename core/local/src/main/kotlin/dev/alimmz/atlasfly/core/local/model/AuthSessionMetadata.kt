package dev.alimmz.atlasfly.core.local.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthSessionMetadata(
    val uid: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
) {
    val hasVerifiedSession: Boolean
        get() = emailVerified && uid.isNotEmpty()
}
