package tech.nullexdev.atlasfly.core.navigation

import kotlinx.serialization.Serializable

object Routes {

    @Serializable
    data object Home

    @Serializable
    data object Flights

    @Serializable
    data object Profile


    object Auth {

        @Serializable
        data object Login

        @Serializable
        data object SignUp

        @Serializable
        data object ForgotPassword
    }
}