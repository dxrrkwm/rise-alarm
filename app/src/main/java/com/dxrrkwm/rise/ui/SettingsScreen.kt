package com.dxrrkwm.rise.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.alarm.AlarmScheduler
import com.dxrrkwm.rise.data.Prefs
import com.dxrrkwm.rise.ring.QrScanner

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    var scanning by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        refresh++
        onPauseOrDispose {}
    }
    val access = remember(refresh) { AccessList.all(context) }

    if (scanning) {
        BackHandler { scanning = false }
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.qr_register_title), style = MaterialTheme.typography.headlineSmall)
            QrScanner(onResult = {
                Prefs.qrCode = it
                scanning = false
                Toast.makeText(context, R.string.qr_saved, Toast.LENGTH_SHORT).show()
            })
            OutlinedButton(onClick = { scanning = false }) { Text(stringResource(R.string.cancel)) }
        }
        return
    }

    var checkDelay by remember { mutableIntStateOf(Prefs.checkDelayMinutes) }
    var checkCount by remember { mutableIntStateOf(Prefs.checkCount) }
    var volume by remember { mutableIntStateOf(Prefs.volumePercent) }
    var maxRing by remember { mutableIntStateOf(Prefs.maxRingMinutes) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Text(stringResource(R.string.tab_settings), style = MaterialTheme.typography.headlineLarge)

        Section(stringResource(R.string.section_access)) {
            access.forEach { AccessRow(it, onClick = { context.startActivity(it.settings) }) }
        }

        Section(stringResource(R.string.section_qr)) {
            Text(
                if (Prefs.qrCode.isEmpty()) stringResource(R.string.qr_none) else stringResource(R.string.qr_current, Prefs.qrCode.take(32)),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = { scanning = true }) { Text(stringResource(R.string.qr_register)) }
        }

        Section(stringResource(R.string.wake_check)) {
            Stepper(stringResource(R.string.check_delay), checkDelay, 1..30, {
                checkDelay = it
                Prefs.checkDelayMinutes = it
            }, format = { context.getString(R.string.minutes, it) })
            Stepper(stringResource(R.string.check_count), checkCount, 0..5, {
                checkCount = it
                Prefs.checkCount = it
            })
        }

        Section(stringResource(R.string.section_volume, volume)) {
            Slider(
                value = volume.toFloat(),
                onValueChange = { volume = (it / 5).toInt() * 5 },
                onValueChangeFinished = { Prefs.volumePercent = volume },
                valueRange = 30f..100f,
            )
            Stepper(stringResource(R.string.max_ring), maxRing, 5..60, {
                maxRing = it
                Prefs.maxRingMinutes = it
            }, format = { context.getString(R.string.minutes, it) })
        }

        OutlinedButton(
            onClick = {
                AlarmScheduler.scheduleTest(context, 10_000)
                Toast.makeText(context, R.string.test_scheduled, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.test_alarm)) }
    }
}

@Composable
private fun AccessRow(access: Access, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (access.granted) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = if (access.granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp),
        )
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(stringResource(access.title), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(access.hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
