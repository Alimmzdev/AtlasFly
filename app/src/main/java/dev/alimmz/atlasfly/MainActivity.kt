package dev.alimmz.atlasfly

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dev.alimmz.atlasfly.app.AtlasFlyApp
import dev.alimmz.atlasfly.app.AtlasFlyViewModel
import dev.alimmz.atlasfly.core.designsystem.theme.AtlasFlyTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: AtlasFlyViewModel by viewModels()
    private var deepLinkUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.isLoading }
        super.onCreate(savedInstanceState)
        // AppCompat restores and persists selections locally (framework storage on Android 13+).
        // Persist the supported device default as well when no selection exists yet.
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            val defaultTag = if (resources.configuration.locales[0].language == "fa") "fa" else "en"
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(defaultTag))
        }
        enableEdgeToEdge()
        deepLinkUri = intent?.data
        setContent {
            AtlasFlyTheme {
                AtlasFlyApp(
                    deepLinkUri = deepLinkUri,
                    viewModel = viewModel,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkUri = intent.data
    }
}
