package dev.alimmz.atlasfly.app

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import dev.alimmz.atlasfly.R
import java.util.Locale

@Composable
fun LanguageSwitcher(modifier: Modifier = Modifier) {
    val selectedTag = currentLanguageTag()
    val isPersian = selectedTag == "fa"
    val switcherDescription = stringResource(R.string.language_switcher_cd)

    Row(
        modifier = modifier
            .semantics { contentDescription = switcherDescription }
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .selectableGroup()
            .padding(4.dp),
    ) {
        LanguageChip(
            label = stringResource(R.string.language_english),
            selected = !isPersian,
            onClick = { setAppLanguage("en") },
        )
        LanguageChip(
            label = stringResource(R.string.language_persian),
            selected = isPersian,
            onClick = { setAppLanguage("fa") },
        )
    }
}

@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

private fun currentLanguageTag(): String {
    val appLocales = AppCompatDelegate.getApplicationLocales()
    val tag = appLocales[0]?.language
    return when {
        !tag.isNullOrBlank() -> tag
        else -> Locale.getDefault().language
    }
}

private fun setAppLanguage(languageTag: String) {
    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(languageTag)
    )
}
