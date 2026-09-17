package dev.alimmz.atlasfly

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabase: SupabaseClient

    private val viewModel: AtlasFlyViewModel by viewModels()
    private var deepLinkUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.isLoading }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtlasFlyTheme {
                AtlasFlyApp(
                    deepLinkUri = deepLinkUri,
                    viewModel = viewModel,
                )
            }
        }
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        val uri = intent.data
        if (uri?.scheme == AUTH_SCHEME && uri.host == AUTH_HOST) {
            supabase.handleDeeplinks(
                intent = intent,
                onSessionSuccess = {
                    deepLinkUri = uri
                    viewModel.onEvent(dev.alimmz.atlasfly.app.AtlasFlyEvent.Refresh)
                },
                onError = { error ->
                    android.util.Log.e("SupabaseAuth", "Auth callback failed", error)
                },
            )
        } else {
            deepLinkUri = uri
        }
    }

    private companion object {
        const val AUTH_SCHEME = "atlasfly"
        const val AUTH_HOST = "auth"
    }
}
