package tech.nullexdev.atlasfly.app

import tech.nullexdev.atlasfly.core.navigation.Routes

data class AtlasFlyUiState(
    val isAuthorized: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val pendingNavigation: Routes? = null,
    val emailVerificationMessage: String? = null,
)