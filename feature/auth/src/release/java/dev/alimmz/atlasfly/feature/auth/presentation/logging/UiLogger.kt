package dev.alimmz.atlasfly.feature.auth.presentation.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

internal object UiLogger {
    @Suppress("UNUSED_PARAMETER")
    fun <T> observeState(scope: CoroutineScope, owner: String, state: StateFlow<T>) = Unit

    @Suppress("UNUSED_PARAMETER")
    fun logEvent(owner: String, event: Any?) = Unit
}
