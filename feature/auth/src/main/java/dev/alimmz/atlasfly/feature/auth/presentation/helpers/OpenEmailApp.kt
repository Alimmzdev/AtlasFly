package dev.alimmz.atlasfly.feature.auth.presentation.helpers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import dev.alimmz.atlasfly.feature.auth.presentation.R

internal fun openEmailApp(context: Context) {
    val inboxIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_EMAIL)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(inboxIntent)
    } catch (_: ActivityNotFoundException) {
        val emailChooserIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(emailChooserIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                R.string.auth_no_email_app,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}
