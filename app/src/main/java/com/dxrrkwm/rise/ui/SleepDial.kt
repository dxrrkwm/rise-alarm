package com.dxrrkwm.rise.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

private const val LAP_MINUTES = 60f
private val LAVENDER = Color(0xFFC9B8F5)

// one turn is an hour, the arc keeps growing in a new colour on every next lap
@Composable
fun SleepDial(
    minutes: Float,
    modifier: Modifier = Modifier,
    max: Int = 180,
    onChange: ((Int) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, LAVENDER)
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val tick = MaterialTheme.colorScheme.outline
    val knob = MaterialTheme.colorScheme.onSurface
    val haptics = LocalHapticFeedback.current
    val value by rememberUpdatedState(minutes)
    val change by rememberUpdatedState(onChange)

    val drag = if (onChange == null) {
        Modifier
    } else {
        Modifier.pointerInput(max) {
            var total = 0f
            var angle = 0f
            detectDragGestures(
                onDragStart = {
                    total = value
                    angle = angleOf(it, size)
                },
                onDrag = { pointer, _ ->
                    val next = angleOf(pointer.position, size)
                    var delta = next - angle
                    if (delta > 180) delta -= 360
                    if (delta < -180) delta += 360
                    angle = next
                    val before = total.roundToInt()
                    total = (total + delta / 360f * LAP_MINUTES).coerceIn(1f, max.toFloat())
                    if (total.roundToInt() != before) {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        change?.invoke(total.roundToInt())
                    }
                },
            )
        }
    }

    Box(modifier.aspectRatio(1f).then(drag), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 22.dp.toPx()
            val radius = size.minDimension / 2 - stroke
            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            repeat(60) { i ->
                val long = i % 5 == 0
                val a = Math.toRadians(i * 6.0 - 90)
                val outer = radius - stroke / 2 - 10.dp.toPx()
                val inner = outer - (if (long) 12.dp else 5.dp).toPx()
                drawLine(
                    color = tick,
                    start = Offset(center.x + outer * cos(a).toFloat(), center.y + outer * sin(a).toFloat()),
                    end = Offset(center.x + inner * cos(a).toFloat(), center.y + inner * sin(a).toFloat()),
                    strokeWidth = (if (long) 2.5.dp else 1.dp).toPx(),
                    cap = StrokeCap.Round,
                )
            }

            drawCircle(track, radius, style = Stroke(stroke))

            val laps = minutes / LAP_MINUTES
            val full = floor(laps).toInt()
            val sweep = (laps - full) * 360f
            val color = colors[full.coerceAtMost(colors.lastIndex)]
            if (full > 0) {
                drawCircle(colors[(full - 1).coerceAtMost(colors.lastIndex)], radius, style = Stroke(stroke))
            }
            drawArc(color, -90f, sweep, false, arcTopLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))

            val a = Math.toRadians(sweep - 90.0)
            val knobCenter = Offset(center.x + radius * cos(a).toFloat(), center.y + radius * sin(a).toFloat())
            drawCircle(knob, stroke * 0.8f, knobCenter)
            drawCircle(color, stroke * 0.45f, knobCenter)
        }
        content()
    }
}

// clockwise angle from 12 o'clock, 0..360
private fun angleOf(point: Offset, size: IntSize): Float {
    val dx = point.x - size.width / 2f
    val dy = point.y - size.height / 2f
    val degrees = Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble())).toFloat()
    return (degrees + 360f) % 360f
}
