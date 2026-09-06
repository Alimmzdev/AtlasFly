package dev.alimmz.atlasfly.feature.auth.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.alimmz.atlasfly.feature.auth.presentation.R

@Composable
internal fun PasswordField(
    value: String,
    error: String?,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onDone: KeyboardActionScope.() -> Unit,
    onForgotPassword: (() -> Unit)? = null,
    label: String? = null,
    placeholder: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onNext: (KeyboardActionScope.() -> Unit)? = null,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val resolvedLabel = label ?: stringResource(R.string.auth_password_label)
    val resolvedPlaceholder = placeholder ?: stringResource(R.string.auth_password_placeholder)

    val onForgot = onForgotPassword
    AuthLabeledField(
        label = resolvedLabel,
        error = error,
        trailingLabel = if (onForgot != null) {
            {
                Text(
                    text = stringResource(R.string.auth_forgot),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = if (enabled) 1f else 0.4f,
                    ),
                    modifier = Modifier
                        .clickable(enabled = enabled, onClick = onForgot)
                        .padding(vertical = 2.dp),
                )
            }
        } else {
            null
        },
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = resolvedPlaceholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                )
            },
            singleLine = true,
            isError = error != null,
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.medium,
            colors = atlasFieldColors(),
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = stringResource(
                            if (isPasswordVisible) {
                                R.string.auth_hide_password
                            } else {
                                R.string.auth_show_password
                            }
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onNext = onNext,
                onDone = onDone,
            ),
        )
    }
}
