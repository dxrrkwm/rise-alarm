package com.dxrrkwm.rise.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dxrrkwm.rise.R
import com.dxrrkwm.rise.alarm.AlarmActions
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.data.AlarmStore

@Composable
fun AlarmsScreen(onEdit: (Alarm) -> Unit) {
    val alarms by AlarmStore.alarms.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val next = alarms.filter { it.enabled }.minOfOrNull { it.nextTrigger() }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(Modifier.padding(start = 4.dp, bottom = 16.dp)) {
                    Text(stringResource(R.string.tab_alarms), style = MaterialTheme.typography.headlineLarge)
                    Text(
                        if (next != null) resources.untilText(next) else stringResource(R.string.no_active_alarms),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (next != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (alarms.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.alarms_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 24.dp),
                    )
                }
            }
            items(alarms, key = { it.id }) { AlarmRow(it, onClick = { onEdit(it) }) }
        }
        FloatingActionButton(
            onClick = { onEdit(Alarm(id = AlarmStore.nextId())) },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.alarm_new))
        }
    }
}

@Composable
private fun AlarmRow(alarm: Alarm, onClick: () -> Unit) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val dim = if (alarm.enabled) 1f else 0.4f

    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(start = 24.dp, end = 20.dp, top = 14.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).alpha(dim)) {
            Text(alarm.timeText(), style = MaterialTheme.typography.displayMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    listOf(resources.daysText(alarm), alarm.label).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                val icons = alarm.missions.map { it.icon }.ifEmpty { listOf(Icons.Rounded.TouchApp) }
                icons.forEach {
                    Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
        Switch(
            checked = alarm.enabled,
            colors = SwitchDefaults.colors(uncheckedBorderColor = MaterialTheme.colorScheme.outline),
            onCheckedChange = { on ->
                val at = AlarmActions.save(context, alarm.copy(enabled = on))
                if (at != null) Toast.makeText(context, context.resources.untilText(at), Toast.LENGTH_SHORT).show()
            },
        )
    }
}
