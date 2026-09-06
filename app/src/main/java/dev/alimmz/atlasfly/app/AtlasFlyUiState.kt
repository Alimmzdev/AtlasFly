package dev.alimmz.atlasfly.app

import androidx.annotation.StringRes
import dev.alimmz.atlasfly.core.navigation.Routes

data class AtlasFlyUiState(
    val isAuthorized: Boolean = false,
    val isLoading: Boolean = true,
    @StringRes val errorMessage: Int? = null,
    val pendingNavigation: Routes? = null,
    @StringRes val emailVerificationMessage: Int? = null,
)
