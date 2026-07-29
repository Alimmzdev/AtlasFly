package tech.nullexdev.atlasfly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import tech.nullexdev.atlasfly.app.AtlasFlyApp
import tech.nullexdev.atlasfly.ui.theme.AtlasFlyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AtlasFlyTheme {
                AtlasFlyApp()
            }
        }
    }
}