package tech.nullexdev.atlasfly.core.navigation

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
        data object SignUp : Auth

        @Serializable
        data object ForgotPassword : Auth
    }
}