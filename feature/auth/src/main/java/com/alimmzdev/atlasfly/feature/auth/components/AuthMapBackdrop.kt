package com.alimmzdev.atlasfly.feature.auth.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthMapBackdrop(modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
    val faint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val stroke = 1.2.dp.toPx()
        val origin = Offset(size.width * 0.92f, size.height * 0.06f)

        drawCircle(
            color = line,
            radius = size.width * 0.62f,
            center = origin,
            style = Stroke(width = stroke),
        )
        drawCircle(
            color = line,
            radius = size.width * 0.38f,
            center = origin,
            style = Stroke(width = stroke),
        )
        drawCircle(
            color = line,
            radius = size.width * 0.18f,
            center = origin,
            style = Stroke(width = stroke),
        )

        val dash = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx()))
        drawLine(
            color = faint,
            start = Offset(0f, size.height * 0.22f),
            end = Offset(size.width, size.height * 0.08f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dash,
        )
    }
}
