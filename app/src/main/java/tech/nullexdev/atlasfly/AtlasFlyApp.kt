package tech.nullexdev.atlasfly

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import tech.nullexdev.atlasfly.core.navigation.Routes

@Composable
fun AtlasFlyApp() {

    val backStack = remember {
        mutableStateListOf<Any>(Routes.Auth.Login)
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = { key ->
            navEntry(key)
        }
    )
}


private fun navEntry(key: Any): NavEntry<Any> {

    return when (key) {

        Routes.Auth.Login -> NavEntry(key) {
            Text("Login Screen")
        }

        Routes.Auth.SignUp -> NavEntry(key) {
            Text("Sign Up Screen")
        }

        Routes.Auth.ForgotPassword -> NavEntry(key) {
            Text("Forgot Password Screen")
        }


        Routes.Home -> NavEntry(key) {
            Text("Home Screen")
        }

        Routes.Flights -> NavEntry(key) {
            Text("Flights Screen")
        }

        Routes.Profile -> NavEntry(key) {
            Text("Profile Screen")
        }

        else -> error("Unknown route: $key")
    }
}