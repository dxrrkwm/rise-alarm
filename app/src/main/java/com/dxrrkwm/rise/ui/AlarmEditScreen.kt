package com.dxrrkwm.rise.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.alarm.AlarmActions
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.data.AlarmStore
import com.dxrrkwm.rise.data.Mission
import com.dxrrkwm.rise.data.Prefs
import com.dxrrkwm.rise.data.Sounds

private val RAMPS = listOf(0, 30, 60, 120)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlarmEditScreen(initial: Alarm, onClose: () -> Unit) {
    val context = LocalContext.current
    var alarm by remember { mutableStateOf(initial) }
    var pickingSound by remember { mutableStateOf(false) }
    val exists = remember { AlarmStore.get(initial.id) != null }
    val levels = stringArrayResource(R.array.levels)

    if (pickingSound) {
        SoundPicker(alarm.sound, onPick = { alarm = alarm.copy(sound = it) }, onClose = { pickingSound = false })
        return
    }

    fun save() {
        val at = AlarmActions.save(context, alarm.copy(enabled = true))
        if (at != null) Toast.makeText(context, context.resources.untilText(at), Toast.LENGTH_SHORT).show()
        onClose()
    }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = null) }
                Spacer(Modifier.weight(1f))
                if (exists) {
                    IconButton(onClick = {
                        AlarmActions.delete(context, initial)
                        onClose()
                    }) { Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.alarm_delete)) }
                }
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    TimeWheel(alarm.hour, alarm.minute) { h, m -> alarm = alarm.copy(hour = h, minute = m) }
                    Text(
                        LocalResources.current.untilText(alarm.nextTrigger()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Section(stringResource(R.string.section_days)) {
                    DayPicker(alarm.days) { day -> alarm = alarm.copy(days = alarm.days.toggle(day)) }
                }

                Section(stringResource(R.string.section_mission)) {
                    MissionPicker(alarm.missions) { alarm = alarm.copy(missions = it) }
                    Text(
                        stringResource(
                            when {
                                alarm.missions.isEmpty() -> R.string.missions_none
                                alarm.missions.size > 1 -> R.string.missions_order
                                else -> R.string.missions_hint
                            },
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (alarm.missions.any { it == Mission.MATH || it == Mission.SHAKE }) {
                        Choice(levels.toList(), alarm.level - 1) { alarm = alarm.copy(level = it + 1) }
                    }
                    if (Mission.QR in alarm.missions && Prefs.qrCode.isEmpty()) {
                        Text(stringResource(R.string.qr_not_set), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }

                Section(stringResource(R.string.section_sound)) {
                    LinkRow(stringResource(R.string.sound), Sounds.title(context, alarm.sound)) { pickingSound = true }
                    Text(stringResource(R.string.section_ramp), style = MaterialTheme.typography.bodyLarge)
                    val ramps = RAMPS.map { if (it == 0) stringResource(R.string.ramp_off) else stringResource(R.string.seconds, it) }
                    Choice(ramps, RAMPS.indexOf(alarm.rampSeconds)) { alarm = alarm.copy(rampSeconds = RAMPS[it]) }
                    SwitchRow(stringResource(R.string.vibration), alarm.vibrate, { alarm = alarm.copy(vibrate = it) })
                }

                Section(stringResource(R.string.section_more)) {
                    SwitchRow(
                        stringResource(R.string.wake_check),
                        alarm.wakeCheck,
                        { alarm = alarm.copy(wakeCheck = it) },
                        hint = stringResource(R.string.wake_check_hint, Prefs.checkDelayMinutes),
                    )
                    OutlinedTextField(
                        value = alarm.label,
                        onValueChange = { alarm = alarm.copy(label = it) },
                        placeholder = { Text(stringResource(R.string.label)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = ::save,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp),
            ) { Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Choice(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, name ->
            FilterChip(
                selected = index == selected,
                onClick = { onSelect(index) },
                label = { Text(name) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
