package com.dxrrkwm.rise.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.alarm.AlarmActions
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.data.AlarmStore

@Composable
fun AlarmsScreen(onEdit: (Alarm) -> Unit) {
    val alarms by AlarmStore.alarms.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.tab_alarms),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                )
            }
            if (alarms.isEmpty()) {
                item {
                    Text(stringResource(R.string.alarms_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(alarms, key = { it.id }) { AlarmCard(it, onClick = { onEdit(it) }) }
        }
        FloatingActionButton(
            onClick = { onEdit(Alarm(id = AlarmStore.nextId())) },
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.alarm_new))
        }
    }
}

@Composable
private fun AlarmCard(alarm: Alarm, onClick: () -> Unit) {
    val context = LocalContext.current
    val missions = stringArrayResource(R.array.missions)
    val details = listOf(LocalResources.current.daysText(alarm), missions[alarm.mission.ordinal], alarm.label)
        .filter { it.isNotBlank() }
        .joinToString(" · ")

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).alpha(if (alarm.enabled) 1f else 0.45f)) {
                Text(alarm.timeText(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Light)
                Text(details, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = { on ->
                    val at = AlarmActions.save(context, alarm.copy(enabled = on))
                    if (at != null) Toast.makeText(context, context.untilText(at), Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}
