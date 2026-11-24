package com.dxrrkwm.rise

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dxrrkwm.rise.data.Alarm
import com.dxrrkwm.rise.ui.AlarmEditScreen
import com.dxrrkwm.rise.ui.AlarmsScreen
import com.dxrrkwm.rise.ui.SettingsScreen
import com.dxrrkwm.rise.ui.SleepScreen
import com.dxrrkwm.rise.ui.theme.RiseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RiseTheme { RiseNav() } }
    }
}

private enum class Tab(@StringRes val title: Int, val icon: ImageVector) {
    ALARMS(R.string.tab_alarms, Icons.Outlined.Alarm),
    SLEEP(R.string.tab_sleep, Icons.Outlined.Bedtime),
    SETTINGS(R.string.tab_settings, Icons.Outlined.Settings),
}

@Composable
private fun RiseNav() {
    var tab by rememberSaveable { mutableStateOf(Tab.ALARMS) }
    var editing by remember { mutableStateOf<Alarm?>(null) }

    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) { notifications.launch(Manifest.permission.POST_NOTIFICATIONS) }

    val alarm = editing
    if (alarm != null) {
        BackHandler { editing = null }
        AlarmEditScreen(alarm, onClose = { editing = null })
        return
    }

    Scaffold(bottomBar = { BottomBar(tab, onSelect = { tab = it }) }) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.ALARMS -> AlarmsScreen(onEdit = { editing = it })
                Tab.SLEEP -> SleepScreen()
                Tab.SETTINGS -> SettingsScreen()
            }
        }
    }
}

@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Tab.entries.forEach {
                val color by animateColorAsState(
                    if (it == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tab",
                )
                Column(
                    Modifier
                        .weight(1f)
                        .clickable(interactionSource = null, indication = null) { onSelect(it) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(it.icon, contentDescription = null, tint = color)
                    Text(stringResource(it.title), style = MaterialTheme.typography.labelSmall, color = color)
                }
            }
        }
    }
}
