package com.dxrrkwm.rise.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.data.Prefs
import com.dxrrkwm.rise.guard.GuardService
import com.dxrrkwm.rise.sleep.SleepTimerService
import kotlinx.coroutines.delay

private val PRESETS = listOf(15, 30, 45, 60, 90, 120)

@Composable
fun SleepScreen() {
    val endsAt by SleepTimerService.endsAt.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(R.string.tab_sleep), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.fillMaxWidth())
        Text(
            stringResource(R.string.sleep_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        val end = endsAt
        if (end != null) Running(end) else Setup()

        if (!GuardService.isEnabled(LocalContext.current)) {
            Text(
                stringResource(R.string.sleep_no_guard),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Setup() {
    val context = LocalContext.current
    var minutes by rememberSaveable { mutableIntStateOf(Prefs.sleepMinutes) }

    SleepDial(minutes.toFloat(), Modifier.fillMaxWidth(0.85f), onChange = { minutes = it }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                durationText(minutes),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light,
            )
            Text(stringResource(R.string.sleep_drag), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), modifier = Modifier.fillMaxWidth()) {
        PRESETS.forEach {
            FilterChip(selected = minutes == it, onClick = { minutes = it }, label = { Text(stringResource(R.string.minutes, it)) })
        }
    }
    Button(
        onClick = {
            Prefs.sleepMinutes = minutes
            SleepTimerService.start(context, minutes)
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) { Text(stringResource(R.string.sleep_start)) }
}

@Composable
private fun Running(end: Long) {
    val context = LocalContext.current
    val left by produceState(end - System.currentTimeMillis(), end) {
        while (true) {
            value = (end - System.currentTimeMillis()).coerceAtLeast(0)
            delay(1000)
        }
    }
    val seconds = left / 1000

    SleepDial(left / 60_000f, Modifier.fillMaxWidth(0.85f)) {
        Text(
            "%d:%02d".format(seconds / 60, seconds % 60),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Light,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(10, 30).forEach {
            OutlinedButton(onClick = { SleepTimerService.extend(context, it) }) { Text(stringResource(R.string.plus_minutes, it)) }
        }
    }
    Button(onClick = { SleepTimerService.cancel(context) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Text(stringResource(R.string.sleep_stop))
    }
}

@Composable
private fun durationText(minutes: Int): String =
    if (minutes < 60) {
        stringResource(R.string.minutes, minutes)
    } else {
        stringResource(R.string.hours_minutes, minutes / 60, minutes % 60)
    }
