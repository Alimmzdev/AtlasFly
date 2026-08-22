package com.alimmzdev.atlasfly.feature.auth.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import tech.nullexdev.atlasfly.feature.auth.presentation.R


@Composable
internal fun AuthHeader() {
    Text(
        text = stringResource(R.string.auth_title),
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold
        )
    )

    Text(
        text = stringResource(R.string.auth_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}