package dev.alimmz.atlasfly.feature.auth.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthMailMark(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier.size(40.dp)) {
        val stroke = 2.dp.toPx()
        val inset = stroke
        val w = size.width - inset * 2
        val h = size.height * 0.72f
        val top = (size.height - h) / 2f
        val left = inset

        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(w, h),
            cornerRadius = CornerRadius(6.dp.toPx()),
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        val flap = Path().apply {
            moveTo(left + stroke / 2, top + 2.dp.toPx())
            lineTo(size.width / 2f, top + h * 0.52f)
            lineTo(left + w - stroke / 2, top + 2.dp.toPx())
        }
        drawPath(
            path = flap,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
