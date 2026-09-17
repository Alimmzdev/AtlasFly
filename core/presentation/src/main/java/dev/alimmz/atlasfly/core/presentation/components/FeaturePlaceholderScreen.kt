package dev.alimmz.atlasfly.core.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.alimmz.atlasfly.core.presentation.R
import dev.alimmz.atlasfly.core.presentation.shell.MainSharedViewModel

/**
 * Stand-in body for a top-level destination that has no feature UI yet.
 *
 * The "notify me" action publishes through [MainSharedViewModel] so the shell
 * surfaces it, whichever tab the user is on.
 */
@Composable
fun FeaturePlaceholderScreen(
    @StringRes titleRes: Int,
    icon: ImageVector,
    sharedViewModel: MainSharedViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = stringResource(R.string.shell_coming_soon),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        OutlinedButton(
            onClick = { sharedViewModel.showMessage(R.string.shell_notify_requested) },
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text(text = stringResource(R.string.shell_notify_me))
        }
    }
}
