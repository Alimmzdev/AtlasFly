package com.alimmzdev.atlasfly.feature.auth.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun WantsToSignUpDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
){
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Create Account")
        },
        text = {
            Text("Do you want to create a new account?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text("Sign Up")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
            ) {
                Text("Cancel")
            }
        }
    )
}