package com.dxrrkwm.rise.ring

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.dxrrkwm.rise.ui.icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.alarm.Phase
import com.dxrrkwm.rise.alarm.RingService
import com.dxrrkwm.rise.alarm.WakeSession
import com.dxrrkwm.rise.data.Mission
import com.dxrrkwm.rise.data.Prefs
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun RingScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val phase by WakeSession.phase.collectAsStateWithLifecycle()

    LaunchedEffect(phase) {
        if (phase == Phase.IDLE || phase == Phase.CHECK_WAIT) onClose()
    }

    Box(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        RingService.quiet()
                    }
                }
            },
    ) {
        when (phase) {
            Phase.RINGING -> MissionScreen(onDone = { WakeSession.advance(context) })
            Phase.CHECK_PROMPT -> CheckScreen(onConfirm = { WakeSession.advance(context) })
            else -> Unit
        }
    }
}

@Composable
private fun MissionScreen(onDone: () -> Unit) {
    val alarm = remember { WakeSession.alarm() }
    val missions = remember { alarm.missions.map { if (it == Mission.QR && Prefs.qrCode.isEmpty()) Mission.MATH else it } }
    var step by rememberSaveable { mutableIntStateOf(0) }

    fun next() {
        if (step >= missions.lastIndex) onDone() else step += 1
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Clock()
        if (alarm.label.isNotBlank()) {
            Text(alarm.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        }
        if (missions.size > 1) Progress(missions, step)
        Spacer(Modifier.height(24.dp))
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (missions.isEmpty()) return@Box DismissButton(onDone)
            key(step) {
                when (missions[step]) {
                    Mission.MATH -> MathMission(level = alarm.level, onDone = ::next)
                    Mission.SHAKE -> ShakeMission(level = alarm.level, onDone = ::next)
                    Mission.QR -> QrMission(expected = Prefs.qrCode, onDone = ::next)
                }
            }
        }
    }
}

@Composable
private fun Progress(missions: List<Mission>, step: Int) {
    Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        missions.forEachIndexed { index, mission ->
            Icon(
                mission.icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = when {
                    index < step -> MaterialTheme.colorScheme.primary
                    index == step -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                },
            )
        }
    }
}

@Composable
private fun DismissButton(onClick: () -> Unit) {
    Box(
        Modifier
            .size(180.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(R.string.ring_dismiss), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun CheckScreen(onConfirm: () -> Unit) {
    val left by produceState(CHECK_SECONDS) {
        while (value > 0) {
            delay(1000)
            value -= 1
        }
    }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(R.string.check_title), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            stringResource(R.string.check_hint, left),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onConfirm, shape = CircleShape, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text(stringResource(R.string.check_confirm), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun Clock() {
    val time by produceState(LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            delay(1000)
        }
    }
    Text(
        time.format(DateTimeFormatter.ofPattern("HH:mm")),
        style = MaterialTheme.typography.displayLarge.copy(fontSize = 88.sp),
        color = MaterialTheme.colorScheme.onSurface,
    )
}

private const val CHECK_SECONDS = 60
