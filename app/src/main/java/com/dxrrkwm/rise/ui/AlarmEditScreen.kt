package com.dxrrkwm.rise.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.alarm.AlarmActions
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.data.AlarmStore
import com.dxrrkwm.rise.data.Mission
import com.dxrrkwm.rise.data.Prefs
import java.time.DayOfWeek

private val RAMPS = listOf(0, 30, 60, 120)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditScreen(initial: Alarm, onClose: () -> Unit) {
    val context = LocalContext.current
    var alarm by remember { mutableStateOf(initial) }
    val time = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
    val exists = remember { AlarmStore.get(initial.id) != null }
    val missions = stringArrayResource(R.array.missions)
    val levels = stringArrayResource(R.array.levels)

    fun save() {
        val at = AlarmActions.save(context, alarm.copy(hour = time.hour, minute = time.minute, enabled = true))
        if (at != null) Toast.makeText(context, context.untilText(at), Toast.LENGTH_SHORT).show()
        onClose()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (exists) R.string.alarm_edit else R.string.alarm_new)) },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = null) }
                },
                actions = {
                    if (exists) {
                        IconButton(onClick = {
                            AlarmActions.delete(context, initial)
                            onClose()
                        }) { Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.alarm_delete)) }
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = ::save,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(56.dp),
            ) { Text(stringResource(R.string.save)) }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TimePicker(state = time, modifier = Modifier.align(Alignment.CenterHorizontally))

            Section(stringResource(R.string.section_days)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayOfWeek.entries.forEach { day ->
                        FilterChip(
                            selected = day in alarm.days,
                            onClick = { alarm = alarm.copy(days = alarm.days.toggle(day)) },
                            label = { Text(context.dayName(day)) },
                        )
                    }
                }
            }

            Section(stringResource(R.string.section_mission)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Mission.entries.forEach { mission ->
                        FilterChip(
                            selected = alarm.mission == mission,
                            onClick = { alarm = alarm.copy(mission = mission) },
                            label = { Text(missions[mission.ordinal]) },
                        )
                    }
                }
                if (alarm.mission == Mission.MATH || alarm.mission == Mission.SHAKE) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        levels.forEachIndexed { index, name ->
                            FilterChip(
                                selected = alarm.level == index + 1,
                                onClick = { alarm = alarm.copy(level = index + 1) },
                                label = { Text(name) },
                            )
                        }
                    }
                }
                if (alarm.mission == Mission.QR && Prefs.qrCode.isEmpty()) {
                    Text(stringResource(R.string.qr_not_set), color = MaterialTheme.colorScheme.error)
                }
            }

            Section(stringResource(R.string.section_ramp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RAMPS.forEach { seconds ->
                        FilterChip(
                            selected = alarm.rampSeconds == seconds,
                            onClick = { alarm = alarm.copy(rampSeconds = seconds) },
                            label = { Text(if (seconds == 0) stringResource(R.string.ramp_off) else stringResource(R.string.seconds, seconds)) },
                        )
                    }
                }
            }

            SwitchRow(stringResource(R.string.vibration), alarm.vibrate, { alarm = alarm.copy(vibrate = it) })
            SwitchRow(
                stringResource(R.string.wake_check),
                alarm.wakeCheck,
                { alarm = alarm.copy(wakeCheck = it) },
                hint = stringResource(R.string.wake_check_hint, Prefs.checkDelayMinutes),
            )

            OutlinedTextField(
                value = alarm.label,
                onValueChange = { alarm = alarm.copy(label = it) },
                label = { Text(stringResource(R.string.label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
