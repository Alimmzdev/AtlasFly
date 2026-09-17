package dev.alimmz.atlasfly

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dev.alimmz.atlasfly.app.AtlasFlyApp
import dev.alimmz.atlasfly.app.AtlasFlyViewModel
import dev.alimmz.atlasfly.core.designsystem.theme.AtlasFlyTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: AtlasFlyViewModel by viewModels()
    private var deepLinkUri by mutableStateOf<Uri?>(null)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

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
        requestChuckerNotificationPermission()
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

    private fun requestChuckerNotificationPermission() {
        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (
            isDebuggable &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
