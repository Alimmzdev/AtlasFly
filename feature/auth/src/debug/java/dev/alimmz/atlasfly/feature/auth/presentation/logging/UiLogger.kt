package dev.alimmz.atlasfly.feature.auth.presentation.logging

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal object UiLogger {
    private const val STATE_TAG = "AtlasFlyUiState"
    private const val EVENT_TAG = "AtlasFlyUiEvent"
    private val secretPattern = Regex(
        pattern = """(?i)((?:password|confirmPassword|oobCode|idToken|accessToken)=|(?:PasswordChanged|ConfirmPasswordChanged)\(value=)([^,)&]*)""",
    )

    fun <T> observeState(scope: CoroutineScope, owner: String, state: StateFlow<T>) {
        scope.launch {
            state.collect { value ->
                Log.d(STATE_TAG, "$owner: ${value.safeForLog()}")
            }
        }
    }

    fun logEvent(owner: String, event: Any?) {
        Log.d(EVENT_TAG, "$owner: ${event.safeForLog()}")
    }

    private fun Any?.safeForLog(): String =
        secretPattern.replace(toString()) { match ->
            "${match.groupValues[1]}<redacted>"
        }
}
