package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NasiOrange
import com.example.ui.theme.NasiPurple
import com.example.ui.theme.NasiYellow

@Composable
fun RiceBowlIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showPlate: Boolean = true,
    showSparkles: Boolean = true,
    showFace: Boolean = true
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = (w * 0.038f).coerceAtLeast(2f)

        // 1. Bottom plate / oval shadow
        if (showPlate) {
            val plateWidth = w * 0.78f
            val plateHeight = h * 0.28f
            val plateX = (w - plateWidth) / 2f
            val plateY = h * 0.58f

            drawOval(
                color = NasiYellow,
                topLeft = Offset(plateX, plateY),
                size = Size(plateWidth, plateHeight),
                style = Fill
            )
            drawOval(
                color = NasiPurple,
                topLeft = Offset(plateX, plateY),
                size = Size(plateWidth, plateHeight),
                style = Stroke(width = strokeW)
            )
        }

        // 2. Rice Mound (top fluffy rice)
        val ricePath = Path().apply {
            val rx = w * 0.22f
            val ry = h * 0.44f
            moveTo(rx, ry)
            // Fluffy scalloped bumps
            cubicTo(w * 0.20f, h * 0.28f, w * 0.32f, h * 0.22f, w * 0.38f, h * 0.26f)
            cubicTo(w * 0.42f, h * 0.16f, w * 0.58f, h * 0.16f, w * 0.62f, h * 0.26f)
            cubicTo(w * 0.68f, h * 0.22f, w * 0.80f, h * 0.28f, w * 0.78f, h * 0.44f)
            close()
        }

        drawPath(path = ricePath, color = Color.White, style = Fill)
        drawPath(
            path = ricePath,
            color = NasiPurple,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Rice grain details inside mound
        val grainColor = NasiPurple.copy(alpha = 0.8f)
        val gSize = w * 0.025f
        drawCircle(grainColor, radius = gSize, center = Offset(w * 0.35f, h * 0.32f))
        drawCircle(grainColor, radius = gSize, center = Offset(w * 0.50f, h * 0.26f))
        drawCircle(grainColor, radius = gSize, center = Offset(w * 0.65f, h * 0.32f))
        drawCircle(grainColor, radius = gSize * 0.9f, center = Offset(w * 0.42f, h * 0.36f))
        drawCircle(grainColor, radius = gSize * 0.9f, center = Offset(w * 0.58f, h * 0.36f))

        // 3. Ceramic Bowl (half circle / trapezoid rounded)
        val bowlPath = Path().apply {
            val leftRim = Offset(w * 0.18f, h * 0.42f)
            val rightRim = Offset(w * 0.82f, h * 0.42f)
            val baseLeft = Offset(w * 0.36f, h * 0.80f)
            val baseRight = Offset(w * 0.64f, h * 0.80f)

            moveTo(leftRim.x, leftRim.y)
            // Left curve down to base
            cubicTo(w * 0.18f, h * 0.68f, w * 0.28f, h * 0.80f, baseLeft.x, baseLeft.y)
            // Base line
            lineTo(baseRight.x, baseRight.y)
            // Right curve up to rim
            cubicTo(w * 0.72f, h * 0.80f, w * 0.82f, h * 0.68f, rightRim.x, rightRim.y)
            close()
        }

        drawPath(path = bowlPath, color = Color.White, style = Fill)
        drawPath(
            path = bowlPath,
            color = NasiPurple,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Bowl foot stand
        val footPath = Path().apply {
            moveTo(w * 0.38f, h * 0.80f)
            lineTo(w * 0.40f, h * 0.85f)
            lineTo(w * 0.60f, h * 0.85f)
            lineTo(w * 0.62f, h * 0.80f)
            close()
        }
        drawPath(path = footPath, color = Color.White, style = Fill)
        drawPath(
            path = footPath,
            color = NasiPurple,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 4. Cute Face (optional)
        if (showFace) {
            val eyeY = h * 0.56f
            val eyeRadius = w * 0.032f
            // Happy closed/open eyes
            drawCircle(color = NasiPurple, radius = eyeRadius, center = Offset(w * 0.40f, eyeY))
            drawCircle(color = NasiPurple, radius = eyeRadius, center = Offset(w * 0.60f, eyeY))

            // Cheerful smile
            val smilePath = Path().apply {
                moveTo(w * 0.46f, h * 0.63f)
                quadraticTo(w * 0.50f, h * 0.69f, w * 0.54f, h * 0.63f)
            }
            drawPath(
                path = smilePath,
                color = NasiPurple,
                style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round)
            )

            // Rosy cheeks
            drawCircle(
                color = NasiOrange.copy(alpha = 0.5f),
                radius = eyeRadius * 1.1f,
                center = Offset(w * 0.32f, eyeY + h * 0.04f)
            )
            drawCircle(
                color = NasiOrange.copy(alpha = 0.5f),
                radius = eyeRadius * 1.1f,
                center = Offset(w * 0.68f, eyeY + h * 0.04f)
            )
        }

        // 5. Sparkle Star
        if (showSparkles) {
            val starCenter = Offset(w * 0.15f, h * 0.34f)
            val starR = w * 0.06f
            val starPath = Path().apply {
                moveTo(starCenter.x, starCenter.y - starR)
                quadraticTo(starCenter.x, starCenter.y, starCenter.x + starR, starCenter.y)
                quadraticTo(starCenter.x, starCenter.y, starCenter.x, starCenter.y + starR)
                quadraticTo(starCenter.x, starCenter.y, starCenter.x - starR, starCenter.y)
                quadraticTo(starCenter.x, starCenter.y, starCenter.x, starCenter.y - starR)
                close()
            }
            drawPath(path = starPath, color = NasiPurple, style = Fill)
        }
    }
}
