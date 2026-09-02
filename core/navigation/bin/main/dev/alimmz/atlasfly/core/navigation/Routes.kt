package dev.alimmz.atlasfly.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes {

    @Serializable
    data object Home : Routes

    @Serializable
    data object Flights : Routes

    @Serializable
    data object Profile : Routes

    @Serializable
    sealed interface Auth : Routes {

        @Serializable
        data object Login : Auth

        @Serializable
        data class SignUpEmailVerification(
            val email: String,
        ) : Auth

        @Serializable
        data class ForgotPassword(
            val email: String = "",
        ) : Auth

        @Serializable
        data class ResetPassword(
            val oobCode: String,
        ) : Auth
    }
}