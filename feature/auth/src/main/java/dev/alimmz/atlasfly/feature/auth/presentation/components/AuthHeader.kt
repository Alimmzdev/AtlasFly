package dev.alimmz.atlasfly.feature.auth.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.alimmz.atlasfly.core.designsystem.brand.AtlasCompassMark
import dev.alimmz.atlasfly.feature.auth.presentation.R

@Composable
internal fun AuthBrandRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AtlasCompassMark(modifier = Modifier.size(26.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.auth_brand),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 20.sp,
                lineHeight = 24.sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
internal fun AuthHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        AuthBrandRow()

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = stringResource(R.string.auth_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.auth_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
