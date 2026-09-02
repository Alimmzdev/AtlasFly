package com.alimmzdev.atlasfly.feature.auth.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun AccountNotFoundDialog(
    email: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Outlined.PersonAddAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = "No account found",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = "We couldn't find an account for $email. Would you like to create one?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}