package dev.alimmz.atlasfly.core.presentation.shell

import androidx.annotation.StringRes

/**
 * State shared by every top-level destination of the app shell.
 *
 * Anything a tab needs to publish to its siblings belongs here rather than in a
 * per-screen state holder.
 */
data class MainSharedUiState(
    @StringRes val message: Int? = null,
)
