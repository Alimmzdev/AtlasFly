package tech.nullexdev.atlasfly.core.designsystem.brand

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.nullexdev.atlasfly.core.designsystem.theme.AtlasFlyTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AtlasCompassMark(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier.size(32.dp)) {
        val stroke = size.minDimension * 0.07f
        val c = center
        val r = size.minDimension / 2f - stroke

        drawCircle(
            color = color,
            radius = r,
            center = c,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = r * 0.58f,
            center = c,
            style = Stroke(width = stroke * 0.65f),
        )

        fun tick(angleDeg: Float, inner: Float, outer: Float, width: Float) {
            val rad = Math.toRadians((angleDeg - 90.0))
            val dx = cos(rad).toFloat()
            val dy = sin(rad).toFloat()
            drawLine(
                color = color,
                start = Offset(c.x + dx * inner, c.y + dy * inner),
                end = Offset(c.x + dx * outer, c.y + dy * outer),
                strokeWidth = width,
                cap = StrokeCap.Round,
            )
        }

        tick(0f, r * 0.78f, r * 0.96f, stroke)
        tick(90f, r * 0.84f, r * 0.96f, stroke * 0.75f)
        tick(180f, r * 0.84f, r * 0.96f, stroke * 0.75f)
        tick(270f, r * 0.84f, r * 0.96f, stroke * 0.75f)

        val needle = Path().apply {
            moveTo(c.x, c.y - r * 0.68f)
            lineTo(c.x + r * 0.12f, c.y + r * 0.1f)
            lineTo(c.x, c.y + r * 0.02f)
            lineTo(c.x - r * 0.12f, c.y + r * 0.1f)
            close()
        }
        drawPath(needle, color)
        drawCircle(color = color, radius = r * 0.08f, center = c)
    }
}

@Preview(showBackground = true)
@Composable
private fun AtlasCompassMarkPreview() {
    AtlasFlyTheme {
        AtlasCompassMark(modifier = Modifier.size(64.dp))
    }
}
