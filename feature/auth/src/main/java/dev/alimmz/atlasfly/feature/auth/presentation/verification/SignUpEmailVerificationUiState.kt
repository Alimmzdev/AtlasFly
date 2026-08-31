package dev.alimmz.atlasfly.feature.auth.presentation.verification

import androidx.annotation.StringRes

data class SignUpEmailVerificationUiState(
    val isLoading: Boolean = false,
    @StringRes val message: Int? = null,
    @StringRes val error: Int? = null,
)
