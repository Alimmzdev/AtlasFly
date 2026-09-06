package dev.alimmz.atlasfly.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes {

    /**
     * Destinations of the signed-in app shell.
     *
     * [TopLevel] entries are the bottom navigation bar tabs; the remaining
     * entries are detail destinations pushed on top of a tab.
     */
    @Serializable
    sealed interface Main : Routes {

        @Serializable
        sealed interface TopLevel : Main {

            @Serializable
            data object Home : TopLevel

            @Serializable
            data object Explore : TopLevel

            @Serializable
            data object Trips : TopLevel

            @Serializable
            data object Planner : TopLevel

            @Serializable
            data object Profile : TopLevel
        }

        @Serializable
        data object SavedPlaces : Main

        @Serializable
        data object SavedTrips : Main

        @Serializable
        data object Preferences : Main

        @Serializable
        data object AccountSettings : Main
    }

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
