package com.dxrrkwm.rise.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dxrrkwm.rise.data.Mission
import java.time.DayOfWeek
import kotlin.math.abs

private const val WHEEL_LOOPS = 400
private val WHEEL_ITEM = 76.dp

// endless snapping wheel, the middle row is the value
@Composable
fun WheelPicker(count: Int, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val start = count * WHEEL_LOOPS / 2 + value
    val state = rememberLazyListState(initialFirstVisibleItemIndex = start - 1)
    val fling = rememberSnapFlingBehavior(state, SnapPosition.Center)
    val haptics = LocalHapticFeedback.current
    val change by rememberUpdatedState(onChange)
    val current by rememberUpdatedState(value)

    val center by remember {
        derivedStateOf {
            val info = state.layoutInfo
            val middle = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - middle) }?.index ?: start
        }
    }
    LaunchedEffect(state) {
        snapshotFlow { center % count }.collect {
            if (it != current) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            change(it)
        }
    }

    LazyColumn(state = state, flingBehavior = fling, modifier = modifier.width(112.dp).height(WHEEL_ITEM * 3)) {
        items(count * WHEEL_LOOPS) { index ->
            val distance = abs(index - center)
            Text(
                "%02d".format(index % count),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                color = if (distance == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WHEEL_ITEM)
                    .graphicsLayer {
                        alpha = if (distance == 0) 1f else 0.25f
                        val scale = if (distance == 0) 1f else 0.8f
                        scaleX = scale
                        scaleY = scale
                    },
            )
        }
    }
}

@Composable
fun TimeWheel(hour: Int, minute: Int, onChange: (Int, Int) -> Unit) {
    val current by rememberUpdatedState(hour to minute)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        WheelPicker(24, hour, onChange = { onChange(it, current.second) })
        Text(":", style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        WheelPicker(60, minute, onChange = { onChange(current.first, it) })
    }
}

@Composable
fun DayPicker(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    val resources = LocalResources.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        DayOfWeek.entries.forEach { day ->
            val on = day in selected
            val background by animateColorAsState(
                if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                label = "day",
            )
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(background).clickable { onToggle(day) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    resources.dayName(day),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// tap to add a mission to the end of the queue, tap again to remove it; the badge is its turn
@Composable
fun MissionPicker(selected: List<Mission>, onChange: (List<Mission>) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Mission.entries.forEach { mission ->
            val order = selected.indexOf(mission)
            val on = order >= 0
            val border by animateColorAsState(
                if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                label = "mission",
            )
            Box(
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, border, MaterialTheme.shapes.medium)
                    .clickable { onChange(if (on) selected - mission else selected + mission) },
            ) {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        mission.icon,
                        contentDescription = null,
                        tint = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(30.dp),
                    )
                    Text(
                        stringResource(mission.title),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (on && selected.size > 1) {
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${order + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}
